package jl95terceira.net;

import static jl95terceira.lang.stt.uncheck;

import jl95terceira.lang.variadic.Function1;
import jl95terceira.lang.variadic.Method0;
import jl95terceira.lang.variadic.Method1;

public abstract class Receiver<T> {

    public static class RecvOptions {
        public Method0            afterStop              = () -> {};
        public Method1<Exception> recvProtocolExcHandler = Throwable::printStackTrace;
        public Method1<Exception> recvHandlingExcHandler = Throwable::printStackTrace;
    }
    public static class RecvAlreadyOnException extends RuntimeException {}

    private final java.io.InputStream  in;
    private       Boolean              recvOn     = false;
    private       Boolean              recvToStop = false;
    private final Object               recvSync   = new Object();

    public Receiver(java.io.InputStream  in) {
        this.in  = in;
    }

    public final void recv             (Method1<T>             incomingCb,
                                        RecvOptions            options) {
        recvWhile((T incoming) -> {
            incomingCb.call(incoming);
            return true;
        }, options);
    }
    public final void recv             (Method1<T>             incomingCb) { recv(incomingCb, new RecvOptions()); }
    synchronized
    public final void recvWhile        (Function1<Boolean, T>  incomingCb,
                                        RecvOptions            options) {
        if (recvOn) {
            throw new RecvAlreadyOnException();
        }
        recvOn     = true;
        recvToStop = false;
        new Thread(() -> {
            synchronized (recvSync) {
                while (!recvToStop) {
                    byte[] incomingAsBytes;
                    try {
                        try {
                            var sizeSize        = in.read();
                            var sizeAsBytes     = new byte[sizeSize];
                            in.read(sizeAsBytes, 0, sizeSize);
                            var size            = new java.math.BigInteger(sizeAsBytes).intValue();
                            incomingAsBytes     = new byte[size];
                            in.read(incomingAsBytes, 0, size);
                        }
                        catch (Exception ex) {
                            options.recvProtocolExcHandler.call(ex);
                            break;
                        }
                        try {
                            var toContinue = incomingCb.call(fromBytes(incomingAsBytes));
                            if (!toContinue) {
                                recvStopAsync();
                            }
                        }
                        catch (Exception ex) {
                            options.recvHandlingExcHandler.call(ex);
                        }
                    }
                    catch (Exception ex) {
                        System.out.println("UNHANDLED FOLLOW-UP EXCEPTION - stop recv");
                        ex.printStackTrace();
                        break;
                    }
                }
                recvOn = false;
            }
            options.afterStop.call();
        }).start();
    }
    public final void recvWhile        (Function1<Boolean, T>  incomingCb) { recvWhile(incomingCb, new RecvOptions()); }
    public final void recvStop         () {
        recvStopAsync();
        synchronized (recvSync) /* wait */ {}
    }
    public final void recvStopAsync    () {
        recvToStop = true;
    }

    protected abstract T fromBytes(byte[] incoming);
}
