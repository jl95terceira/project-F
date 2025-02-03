package jl95.rpc;

import static jl95.lang.SuperPowers.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import jl95.lang.variadic.*;
import jl95.net.BytesReceiver;
import jl95.net.BytesSender;
import jl95.net.Receiver;
import jl95.net.Sender;
import jl95.net.Server;
import jl95.net.util.Defaults;
import jl95.net.util.Util;
import jl95.rpc.protocol.Response;
import jl95.rpc.serdes.ResponseJsonSerdes;
import jl95.rpc.serdes.RequestJsonSerdes;
import jl95.rpc.util.SerdesDefaults;

public abstract class Responder<A, R> {

    public interface    Options {

        InputStream  getInput            ();
        OutputStream getOutput           ();

        class Editable implements Options {

            public Function0<InputStream>  inputGetter;
            public Function0<OutputStream> outputGetter;

            @Override public InputStream  getInput () {
                return inputGetter .call();
            }
            @Override public OutputStream getOutput() {
                return outputGetter.call();
            }

            public void setIoFromSocket(Socket            socket) {
                inputGetter  = unchecked(socket::getInputStream);
                outputGetter = unchecked(socket::getOutputStream);
            }
            public void setIoAsClient  (InetSocketAddress addr) {
                var socket = new Socket();
                System.out.println("about to connect");
                uncheck(() -> socket.setSoTimeout(Defaults.acceptTimeoutMsForSingleClient));
                uncheck(() -> socket.connect(addr));
                System.out.println("about to set IO from socket as client");
                setIoFromSocket(socket);
            }
            public void setIoAsServer  (InetSocketAddress addr,
                                        Optional<Integer> clientConnectionTimeoutMs) {
                var serverOptions = new Server.Options.Editable();
                var clientSocketFuture = new CompletableFuture<Socket>();
                serverOptions.acceptCb = (self, socket) -> {
                    clientSocketFuture.complete(socket);
                };
                var server = new Server(Util.getSimpleServerSocket(addr, Defaults.acceptTimeoutMs), serverOptions);
                server.start();
                setIoFromSocket(uncheck(() -> clientConnectionTimeoutMs.isPresent()
                                            ? clientSocketFuture.get(clientConnectionTimeoutMs.get(), TimeUnit.MILLISECONDS)
                                            : clientSocketFuture.get()));
            }
        }
        static Options defaultsAsServer() {
            var options = new Editable();
            options.setIoAsServer(Defaults.serverAddr, Optional.of(Defaults.acceptTimeoutMsForSingleClient));
            return options;
        }
        static Options defaultsAsClient() {
            var options = new Editable();
            options.setIoAsClient(Defaults.serverAddr);
            return options;
        }
    }
    public static class StartWhenAlreadyOnException extends RuntimeException {}
    public static class StopWhenNotOnException      extends RuntimeException {}

    private final Receiver<byte[]> receiver;
    private final Sender  <byte[]> sender;
    private final Method0          onClose;
    private       Boolean          isOn = false;

    protected abstract A      readRequest  (byte[] serial);
    protected abstract byte[] writeResponse(R      object);

    public Responder(Options options) {

        this.receiver = new BytesReceiver(options::getInput);
        this.sender   = new BytesSender  (options::getOutput);
        onClose = () -> {
            uncheck(options.getInput ()::close);
            uncheck(options.getOutput()::close);
        };
    }

    synchronized public void         start(Function1<R, A> responseFunction) {

        if (isOn) throw new StartWhenAlreadyOnException();
        receiver.recvWhile(serial -> {

            var request = RequestJsonSerdes.fromJson
                         (SerdesDefaults   .jsonFromString .call
                         (SerdesDefaults   .stringFromBytes.call(serial)));
            var requestPayloadObject = readRequest(request.payload);
            var responsePayloadObject   = responseFunction.call(requestPayloadObject);
            var response       = new Response();
            response.id        = UUID.randomUUID();
            response.requestId = request.id;
            response.payload   = writeResponse(responsePayloadObject);
            sender.send(SerdesDefaults .stringToBytes.call
                       (SerdesDefaults .jsonToString .call
                       (ResponseJsonSerdes.toJson(response))));
            return true;
        });
        isOn = true;
    }
    synchronized public Future<Void> stop () {

        if (!isOn) throw new StopWhenNotOnException();
        return receiver.recvStop();
    }
    synchronized public void         close() {
        onClose.call();
    }
}
