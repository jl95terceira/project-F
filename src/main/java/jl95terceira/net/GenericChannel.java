package jl95terceira.net;

import java.util.function.Consumer;

public abstract class GenericChannel<T> implements Channel<T> {

    public static class SendException extends RuntimeException { public SendException(Exception x) {super(x);} }
    public static class RecvException extends RuntimeException { public RecvException(Exception x) {super(x);} }

    private final java.io.OutputStream out;
    private final java.io.InputStream  in;
    private       Boolean              recvToStop = false;
    private final Object               recvSync   = new Object();

    private Consumer<SendException> sendExcHandler;
    private Consumer<RecvException> recvExcHandler;

    public GenericChannel(java.io.InputStream  in,
                          java.io.OutputStream out) {
        this.in  = in;
        this.out = out;
    }

    public final void send             (T                       outgoing) {
        try {
            var outgoingAsBytes = toBytes(outgoing);
            var size            = outgoingAsBytes.length;
            var sizeAsBytes     = java.math.BigInteger.valueOf(size).toByteArray();
            out.write(sizeAsBytes.length);
            out.write(sizeAsBytes);
            out.write(outgoingAsBytes);
        }
        catch (Exception ex) {
            if (sendExcHandler != null) {
                sendExcHandler.accept(new SendException(ex));
            }
            else {

            }
        }
    }
    public final void setSendExcHandler(Consumer<SendException> handler) {
        sendExcHandler = handler;
    }
    public final void recv             (Consumer<T>             incomingCb) {
        recvToStop = false;
        new Thread(() -> {
            synchronized (recvSync) {
                try {
                    while (!recvToStop) {
                        var sizeSize        = in.read();
                        var sizeAsBytes     = new byte[sizeSize];
                        in.read(sizeAsBytes, 0, sizeSize);
                        var size            = new java.math.BigInteger(sizeAsBytes).intValue();
                        var incomingAsBytes = new byte[size];
                        in.read(incomingAsBytes, 0, size);
                        incomingCb.accept(fromBytes(incomingAsBytes));
                    }
                }
                catch (Exception ex) {
                    recvExcHandler.accept(new RecvException(ex));
                }
            }
        }).start();
    }
    public final void recvStop         () {
        recvStopAsync();
        synchronized (recvSync) /* wait */ {}
    }
    public final void recvStopAsync    () {
        recvToStop = true;
    }
    public final void setRecvExcHandler(Consumer<RecvException> handler) {
        recvExcHandler = handler;
    }

    protected abstract byte[] toBytes  (T      outgoing);
    protected abstract T      fromBytes(byte[] incoming);
}
