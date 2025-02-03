package jl95.net;

import static jl95.lang.SuperPowers.constant;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import jl95.lang.variadic.*;

public abstract class Receiver<T> {

    public interface RecvOptions<T> {

        void afterStop          (Receiver<T> receiver);
        void onException        (Receiver<T> receiver, Exception   ex);
        void onIoException      (Receiver<T> receiver, IOException ex);
        void onProtocolException(Receiver<T> receiver, Exception   ex);

        class Editable<T> implements RecvOptions<T> {

            public Method1<Receiver<T>>              afterStop          = (self) -> {};
            public Method2<Receiver<T>, Exception>   excHandler         = (self, ex) -> ex.printStackTrace();
            public Method2<Receiver<T>, IOException> ioExcHandler       = (self, ex) -> ex.printStackTrace();
            public Method2<Receiver<T>, Exception>   protocolExcHandler = (self, ex) -> ex.printStackTrace();

            @Override public void afterStop          (Receiver<T> self) { afterStop.call(self); }
            @Override public void onException        (Receiver<T> self, Exception   ex) { excHandler        .call(self, ex); }
            @Override public void onIoException      (Receiver<T> self, IOException ex) { ioExcHandler      .call(self, ex); }
            @Override public void onProtocolException(Receiver<T> self, Exception   ex) { protocolExcHandler.call(self, ex); }
        }
        static <T> RecvOptions<T> defaults() {
            return new Editable<>();
        }
    }

    public static class StartWhenAlreadyOnException extends RuntimeException {}
    public static class StopWhenNotOnException      extends RuntimeException {}

    private final Function0<java.io.InputStream> inputGetter;
    private       Boolean                        isOn        = false;
    private       Boolean                        toStop      = false;
    private       CompletableFuture<Void>        stopFuture;

    protected abstract T fromBytes(byte[] incoming);

    public Receiver(Function0<java.io.InputStream> inputGetter) {
        this.inputGetter = inputGetter;
    }

    public final void         recv     (Method1<T>            incomingCb,
                                        RecvOptions<T>        options) {
        recvWhile((T incoming) -> {
            incomingCb.call(incoming);
            return true;
        }, options);
    }
    public final void         recv     (Method1<T>            incomingCb) { recv(incomingCb, RecvOptions.defaults()); }
    synchronized
    public final void         recvWhile(Function1<Boolean, T> incomingCb,
                                        RecvOptions<T>        options) {
        if (isOn) {
            throw new StartWhenAlreadyOnException();
        }
        toStop     = false;
        new Thread(() -> {
            while (!toStop) {
                byte[] incomingAsBytes;
                try {
                    try {
                        var input           = inputGetter.call();
                        var sizeSize        = input.read();
                        var sizeAsBytes     = new byte[sizeSize];
                        input.read(sizeAsBytes, 0, sizeSize);
                        var size            = new java.math.BigInteger(sizeAsBytes).intValue();
                        incomingAsBytes     = new byte[size];
                        input.read(incomingAsBytes, 0, size);
                    }
                    catch (IOException ex) {
                        options.onIoException(this, ex);
                        break;
                    }
                    catch (Exception ex) {
                        options.onProtocolException(this, ex);
                        break;
                    }
                    try {
                        var toContinue = incomingCb.call(fromBytes(incomingAsBytes));
                        if (!toContinue) {
                            recvStop();
                        }
                    }
                    catch (Exception ex) {
                        options.onException(this, ex);
                    }
                }
                catch (Exception ex) {
                    System.out.println("UNHANDLED FOLLOW-UP EXCEPTION - stop recv");
                    ex.printStackTrace();
                    break;
                }
            }
            isOn = false;
            assert stopFuture != null;
            stopFuture.completeAsync(() -> null);
            options.afterStop(this);
        }).start();
        stopFuture = new CompletableFuture<>();
        isOn       = true;
    }
    public final void         recvWhile(Function1<Boolean, T> incomingCb) { recvWhile(incomingCb, RecvOptions.defaults()); }
    synchronized
    public final Future<Void> recvStop () {

        if (!isOn) {
            throw new StopWhenNotOnException();
        }
        toStop = true; // to be checked in loop, after which the future above will be completed
        assert stopFuture != null;
        return stopFuture;
    }
    public final <T2> Receiver<T2> extend(Function1<T2, T> adapterFunction) {

        return new Receiver<T2>(inputGetter) {

            @Override protected T2 fromBytes(byte[] incoming) {
                return adapterFunction.call(Receiver.this.fromBytes(incoming));
            }
        };
    }
}
