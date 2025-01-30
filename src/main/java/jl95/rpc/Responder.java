package jl95.rpc;

import static jl95.lang.SuperPowers.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Future;

import jl95.lang.variadic.*;
import jl95.net.BytesReceiver;
import jl95.net.BytesSender;
import jl95.net.Receiver;
import jl95.net.Sender;
import jl95.rpc.protocol.Response;
import jl95.rpc.serdes.ResponseJsonSerdes;
import jl95.rpc.serdes.RequestJsonSerdes;
import jl95.rpc.util.SerdesDefaults;

public abstract class Responder<A, R> {

    public interface    Options {
        InputStream  getInput            ();
        OutputStream getOutput           ();
    }
    public static class EditableOptions1 implements Options {

        public InputStream  input;
        public OutputStream output;

        @Override public InputStream  getInput () {
            return input;
        }
        @Override public OutputStream getOutput() {
            return output;
        }
    }
    public static class EditableOptions2 implements Options {

        public InputStream  input;
        public OutputStream output;

        @Override public InputStream  getInput () {
            return input;
        }
        @Override public OutputStream getOutput() {
            return output;
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

        var input  = options.getInput ();
        var output = options.getOutput();
        this.receiver = new BytesReceiver(input);
        this.sender   = new BytesSender  (output);
        onClose = () -> {
            uncheck(input ::close);
            uncheck(output::close);
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
