package jl95.rpc;

import static jl95.lang.SuperPowers.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jl95.lang.variadic.*;
import jl95.net.BytesReceiver;
import jl95.net.BytesSender;
import jl95.net.Receiver;
import jl95.net.Sender;
import jl95.net.Server;
import jl95.net.util.Defaults;
import jl95.net.util.Util;
import jl95.rpc.protocol.Request;
import jl95.rpc.protocol.Response;
import jl95.rpc.serdes.RequestJsonSerdes;
import jl95.rpc.serdes.ResponseJsonSerdes;
import jl95.rpc.util.RequesterEditableOptionsPartial;
import jl95.rpc.util.SerdesDefaults;

public abstract class Requester<A, R> {

    private enum         ResponseExceptionalStatus {
        FAIL_UNSYNCHRONIZED,
        FAIL_TIMEOUT;
    }
    private static class ResponseStatusAndData {
        public ResponseExceptionalStatus status   = null;
        public Response                  response;
    }

    public interface    Options {

        OutputStream getOutput           ();
        InputStream  getInput            ();
        Integer      getResponseTimeoutMs();

        class Editable extends RequesterEditableOptionsPartial implements Options {

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
                uncheck(() -> socket.connect(addr));
                setIoFromSocket(socket);
            }
            public void setIoAsServer  (InetSocketAddress addr,
                                        Optional<Integer> clientConnectionTimeoutMs) {
                var serverOptions = new Server.Options.Editable();
                var clientSocketFuture = new CompletableFuture<Socket>();
                serverOptions.acceptCb = (self, socket) -> {
                    clientSocketFuture.complete(socket);
                    self.stop();
                };
                var server = new Server(Util.getSimpleServerSocket(addr, Defaults.acceptTimeoutMs), serverOptions);
                System.out.println("about to accept");
                server.start();
                System.out.println("about to set IO from socket as server");
                setIoFromSocket(uncheck(() -> clientConnectionTimeoutMs.isPresent()
                                            ? clientSocketFuture.get(clientConnectionTimeoutMs.get(), TimeUnit.MILLISECONDS)
                                            : clientSocketFuture.get()));
            }
        }
        static Requester.Options defaultsAsServer() {
            var options = new Editable();
            options.setIoAsServer(Defaults.serverAddr, Optional.of(Defaults.acceptTimeoutMsForSingleClient));
            return options;
        }
        static Requester.Options defaultsAsClient() {
            var options = new Editable();
            options.setIoAsClient(Defaults.serverAddr);
            return options;
        }
    }
    public static class OptionsException         extends RuntimeException {
        public OptionsException(String info) {super(info);}
    }
    public static class ResponseTimeoutException extends RuntimeException {}
    public static class ResponseDesynchException extends RuntimeException {}

    private final Sender  <byte[]>         sender;
    private final Receiver<byte[]>         receiver;
    private final Integer                  responseTimeoutMs;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Method0                  onClose;

    protected abstract byte[] writeRequest(A      object);
    protected abstract R      readResponse(byte[] serial);

    public Requester(Options options) {

        this.sender            = new BytesSender  (options::getOutput);
        this.receiver          = new BytesReceiver(options::getInput);
        this.responseTimeoutMs = options.getResponseTimeoutMs();
        onClose = () -> {
            uncheck(options.getInput ()::close);
            uncheck(options.getOutput()::close);
        };
    }

    synchronized public R    call (A requestObject) {

        var request     = new Request();
        request.id      = UUID.randomUUID();
        request.payload = writeRequest(requestObject);
        var requestSerial = SerdesDefaults   .stringToBytes.call
                           (SerdesDefaults   .jsonToString .call
                           (RequestJsonSerdes.toJson(request)));
        sender.send(requestSerial);
        var responseSync   = new Object();
        var responseFuture = new CompletableFuture<ResponseStatusAndData>();
        receiver.recvWhile(resSerial -> {
            synchronized (responseSync) {
                if (responseFuture.isDone()) /* oof, just timed out */ return false;
                var response = ResponseJsonSerdes.fromJson
                              (SerdesDefaults.jsonFromString .call
                              (SerdesDefaults.stringFromBytes.call(resSerial)));
                var rsd = new ResponseStatusAndData();
                if (!response.requestId.equals(request.id)) {
                    rsd.status = ResponseExceptionalStatus.FAIL_UNSYNCHRONIZED;
                }
                else {
                    rsd.response = response;
                }
                responseFuture.complete(rsd);
                return false;
            }
        });
        scheduler.schedule(() -> {
            synchronized (responseSync) {
                if (responseFuture.isDone()) return;
                // not completed - set failed (by time-out)
                var rsd = new ResponseStatusAndData();
                rsd.status = ResponseExceptionalStatus.FAIL_TIMEOUT;
                responseFuture.complete(rsd);
            }
        }, responseTimeoutMs, TimeUnit.MILLISECONDS);
        var rsd = uncheck(() -> responseFuture.get());
        if (rsd.status == ResponseExceptionalStatus.FAIL_TIMEOUT) {
            throw new ResponseTimeoutException();
        }
        if (rsd.status == ResponseExceptionalStatus.FAIL_UNSYNCHRONIZED) {
            throw new ResponseDesynchException();
        }
        var reponseObject = readResponse(rsd.response.payload);
        return reponseObject;
    }
    synchronized public void close() {
        onClose.call();
    }
}
