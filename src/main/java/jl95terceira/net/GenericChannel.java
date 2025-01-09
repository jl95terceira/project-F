package jl95terceira.net;

import jl95terceira.lang.variadic.*;

public abstract class GenericChannel<T> {

    public static class SendException extends RuntimeException { public SendException(Exception x) {super(x);} }
    public static class RecvException extends RuntimeException { public RecvException(Exception x) {super(x);} }

    private final java.io.OutputStream out;
    private final java.io.InputStream  in;
    private       Boolean              recvToStop = false;
    private final Object               recvSync   = new Object();

    private Method1<SendException> sendExcHandler;
    private Method1<RecvException> recvExcHandler;

    public GenericChannel(java.io.InputStream  in,
                          java.io.OutputStream out) {
        this.in  = in;
        this.out = out;
    }

    public final void send             (T                      outgoing) {
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
                sendExcHandler.call(new SendException(ex));
            }
            else {

            }
        }
    }
    public final void setSendExcHandler(Method1<SendException> handler) {
        sendExcHandler = handler;
    }
    public final void recv             (Method1<T>             incomingCb) {
        recv((T incoming) -> {
            incomingCb.call(incoming);
            return true;
        });
    }
    public final void recv             (Function1<Boolean, T>  incomingCb) {
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
                        var toContinue = incomingCb.call(fromBytes(incomingAsBytes));
                        if (!toContinue) {
                            recvToStop = true;
                        }
                    }
                }
                catch (Exception ex) {
                    recvExcHandler.call(new RecvException(ex));
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
    public final void setRecvExcHandler(Method1<RecvException> handler) {
        recvExcHandler = handler;
    }

    protected abstract byte[] toBytes  (T      outgoing);
    protected abstract T      fromBytes(byte[] incoming);
}
