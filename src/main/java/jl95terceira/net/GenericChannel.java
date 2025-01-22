package jl95terceira.net;

import static jl95terceira.lang.stt.uncheck;

import jl95terceira.lang.variadic.*;

public abstract class GenericChannel<T> {

    public static class RecvOptions {
        public Method0            afterStop      = () -> {};
        public Method1<Exception> recvExcHandler = ex -> { ex.printStackTrace(); };
    }
    public static class RecvAlreadyOnException extends RuntimeException {}

    private final java.io.OutputStream out;
    private final java.io.InputStream  in;
    private       Boolean              recvOn     = false;
    private       Boolean              recvToStop = false;
    private final Object               recvSync   = new Object();

    public GenericChannel(java.io.InputStream  in,
                          java.io.OutputStream out) {
        this.in  = in;
        this.out = out;
    }

    public final void       send             (T                      outgoing) {
        var outgoingAsBytes = toBytes(outgoing);
        var size            = outgoingAsBytes.length;
        var sizeAsBytes     = java.math.BigInteger.valueOf(size).toByteArray();
        uncheck(() -> {
            out.write(sizeAsBytes.length);
            out.write(sizeAsBytes);
            out.write(outgoingAsBytes);
        });
    }
    public final void       recv             (Method1<T>             incomingCb,
                                              RecvOptions            options) {
        recvWhile((T incoming) -> {
            incomingCb.call(incoming);
            return true;
        }, options);
    }
    public final void       recv             (Method1<T>             incomingCb) { recv(incomingCb, new RecvOptions()); }
    synchronized
    public final void       recvWhile        (Function1<Boolean, T>  incomingCb,
                                              RecvOptions            options) {
        if (recvOn) {
            throw new RecvAlreadyOnException();
        }
        recvOn     = true;
        recvToStop = false;
        new Thread(() -> {
            synchronized (recvSync) {
                while (!recvToStop) {
                    try {
                        var sizeSize        = in.read();
                        var sizeAsBytes     = new byte[sizeSize];
                        in.read(sizeAsBytes, 0, sizeSize);
                        var size            = new java.math.BigInteger(sizeAsBytes).intValue();
                        var incomingAsBytes = new byte[size];
                        in.read(incomingAsBytes, 0, size);
                        var toContinue = incomingCb.call(fromBytes(incomingAsBytes));
                        if (!toContinue) {
                            recvStopAsync();
                        }
                    }
                    catch (Exception ex) {
                        options.recvExcHandler.call(ex);
                    }
                }
                recvOn = false;
            }
            options.afterStop.call();
        }).start();
    }
    public final void       recvWhile        (Function1<Boolean, T>  incomingCb) { recvWhile(incomingCb, new RecvOptions()); }
    public final void       recvStop         () {
        recvStopAsync();
        synchronized (recvSync) /* wait */ {}
    }
    public final void       recvStopAsync    () {
        recvToStop = true;
    }

    protected abstract byte[] toBytes  (T      outgoing);
    protected abstract T      fromBytes(byte[] incoming);
}
