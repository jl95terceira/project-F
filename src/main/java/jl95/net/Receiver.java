package jl95.net;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import jl95.lang.variadic.*;

public abstract class Receiver<T> {

    public interface    Options {
        void afterStop          ();
        void onException        (Exception   ex);
        void onIoException      (IOException ex);
        void onProtocolException(Exception   ex);
    }
    public static class EditableOptions             implements Options {

        public Method0              afterStop          = () -> {};
        public Method1<Exception>   excHandler         = Throwable::printStackTrace;
        public Method1<IOException> ioExcHandler       = Throwable::printStackTrace;
        public Method1<Exception>   protocolExcHandler = Throwable::printStackTrace;

        @Override public void afterStop          () { }
        @Override public void onException        (Exception   ex) { }
        @Override public void onIoException      (IOException ex) { }
        @Override public void onProtocolException(Exception   ex) { }
    }
    public static class StartWhenAlreadyOnException extends    RuntimeException {}
    public static class StopWhenNotOnException      extends    RuntimeException {}

    private final java.io.InputStream     input;
    private       Boolean                 isOn = false;
    private       Boolean                 toStop = false;
    private       CompletableFuture<Void> stopFuture;

    protected abstract T fromBytes(byte[] incoming);

    public Receiver(java.io.InputStream input) {
        this.input = input;
    }

    public final void recv     (Method1<T>            incomingCb,
                                Options               options) {
        recvWhile((T incoming) -> {
            incomingCb.call(incoming);
            return true;
        }, options);
    }
    public final void recv     (Method1<T>            incomingCb) { recv(incomingCb, new EditableOptions()); }
    synchronized
    public final void recvWhile(Function1<Boolean, T> incomingCb,
                                Options               options) {
        if (isOn) {
            throw new StartWhenAlreadyOnException();
        }
        toStop     = false;
        new Thread(() -> {
            while (!toStop) {
                byte[] incomingAsBytes;
                try {
                    try {
                        var sizeSize        = input.read();
                        var sizeAsBytes     = new byte[sizeSize];
                        input.read(sizeAsBytes, 0, sizeSize);
                        var size            = new java.math.BigInteger(sizeAsBytes).intValue();
                        incomingAsBytes     = new byte[size];
                        input.read(incomingAsBytes, 0, size);
                    }
                    catch (IOException ex) {
                        options.onIoException(ex);
                        break;
                    }
                    catch (Exception ex) {
                        options.onProtocolException(ex);
                        break;
                    }
                    try {
                        var toContinue = incomingCb.call(fromBytes(incomingAsBytes));
                        if (!toContinue) {
                            recvStop();
                        }
                    }
                    catch (Exception ex) {
                        options.onException(ex);
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
            stopFuture.completeAsync(null);
            options.afterStop();
        }).start();
        stopFuture = new CompletableFuture<>();
        isOn       = true;
    }
    public final void recvWhile(Function1<Boolean, T> incomingCb) { recvWhile(incomingCb, new EditableOptions()); }
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
        return new Receiver<T2>(input) {

            @Override protected T2 fromBytes(byte[] incoming) {
                return adapterFunction.call(Receiver.this.fromBytes(incoming));
            }
        };
    }
}
