package jl95.rpc;

import static jl95.lang.SuperPowers.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
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
import jl95.pubsub.util.Defaults;
import jl95.rpc.protocol.Request;
import jl95.rpc.protocol.Response;
import jl95.rpc.serdes.RequestJsonSerdes;
import jl95.rpc.serdes.ResponseJsonSerdes;
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

    public interface             Options {
        OutputStream getOutput           ();
        InputStream  getInput            ();
        Long         getResponseTimeoutMs();
    }
    public static class          OptionsException         extends    RuntimeException {
        public OptionsException(String info) {super(info);}
    }
    public static abstract class EditableOptionsPartial   implements Options {

        public Long responseTimeoutMs = 5000L;

        @Override public Long getResponseTimeoutMs() { return responseTimeoutMs; }
    }
    public static class          EditableOptions1         extends    EditableOptionsPartial
                                                          implements Options {

        public InputStream  input;
        public OutputStream output;

        @Override public InputStream  getInput () {
            return input;
        }
        @Override public OutputStream getOutput() {
            return output;
        }
    }
    public static class          EditableOptions2         extends    EditableOptionsPartial
                                                          implements Options {

        public Socket socket = new Socket();

        @Override public InputStream  getInput () { return uncheck(socket::getInputStream); }
        @Override public OutputStream getOutput() { return uncheck(socket::getOutputStream); }
    }
    public static class          EditableOptions3         extends    EditableOptionsPartial
                                                          implements Options {
        private final Socket  socket    = new Socket();
        private       Boolean connected = false;

        private void connectIfNotConnected() {
            if (connected) return;
            uncheck(() -> socket.connect(responderAddr));
            connected = true;
        }

        public InetSocketAddress responderAddr = Defaults.brokerAddr;

        @Override public InputStream  getInput () { connectIfNotConnected(); return uncheck(socket::getInputStream); }
        @Override public OutputStream getOutput() { connectIfNotConnected(); return uncheck(socket::getOutputStream); }
    }
    public static class          ResponseTimeoutException extends    RuntimeException {}
    public static class          ResponseDesynchException extends    RuntimeException {}

    private final Sender  <byte[]>         sender;
    private final Receiver<byte[]>         receiver;
    private final Long                     responseTimeoutMs;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Method0                  onClose;

    protected abstract byte[] writeRequest(A      object);
    protected abstract R      readResponse(byte[] serial);

    public Requester(Options options) {

        // get options
        var input  = options.getInput ();
        var output = options.getOutput();
        // validate options
        var missing = I(
            tuple("input stream" , input),
            tuple("output stream", output)
        ).filter(t -> t.a2 == null).map(t -> t.a1).toList();
        if (!missing.isEmpty()) {
            throw new OptionsException("the following");
        }
        // ...
        this.sender            = new BytesSender  (output);
        this.receiver          = new BytesReceiver(input);
        this.responseTimeoutMs = options.getResponseTimeoutMs();
        onClose = () -> {
            uncheck(input ::close);
            uncheck(output::close);
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
