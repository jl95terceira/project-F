package jl95.net;

import static jl95.lang.SuperPowers.uncheck;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;

import jl95.lang.variadic.*;

public class Server {

    public interface Options {
        void         onAccept       (Server self, Socket    clientSocket);
        void         onAcceptError  (Server self, Exception ex);
        void         onAcceptTimeout(Server self);

        class Editable implements Options {

            public Method2<Server, Socket>    acceptCb        = (server, socket) -> uncheck(() -> {
                System.out.printf(String.format("Got new connection: %s\nConnection callback method is not overridden\nClose connection", socket));
                socket.close();
            });
            public Method2<Server, Exception> acceptErrorCb   = (server, ex)     -> System.out.printf("Error on accept connection: %s%n", ex);;
            public Method1<Server>            acceptTimeoutCb = (server)         -> {};

            @Override public void onAccept       (Server self, Socket    clientSocket) { acceptCb       .call(self, clientSocket); }
            @Override public void onAcceptError  (Server self, Exception ex)           { acceptErrorCb  .call(self, ex); }
            @Override public void onAcceptTimeout(Server self)                         { acceptTimeoutCb.call(self); }
        }
        static Options defaults() { return new Editable(); }
    }

    private final ServerSocket               serverSocket;
    private final Method2<Server, Socket>    acceptCb;
    private final Method2<Server, Exception> acceptErrorCb;
    private final Method1<Server>            acceptTimeoutCb;
    private final Object                     sync      = new Object();
    private       Boolean                    isRunning = false;
    private       Boolean                    toStop    = false;
    private       CompletableFuture<Void>    stopFuture;


    public Server(ServerSocket socket,
                  Options      options) {
        this.serverSocket    = socket;
        this.acceptCb        = options::onAccept;
        this.acceptErrorCb   = options::onAcceptError;
        this.acceptTimeoutCb = options::onAcceptTimeout;
    }

    synchronized
    public final void         start() {

        if (isRunning) throw new IllegalStateException();
        toStop = false;
        stopFuture = new CompletableFuture<>();
        new Thread(() -> {
            while (!toStop) {
                try {
                    java.net.Socket socket;
                    try {
                        synchronized (sync) {
                            socket = serverSocket.accept();
                        }
                    }
                    catch (java.net.SocketTimeoutException ex) /* not really an error - just to give control back to the thread every so often */ {
                        acceptTimeoutCb.call(this);
                        continue;
                    }
                    catch (Exception ex) {
                        acceptErrorCb.call(this, ex);
                        continue;
                    }
                    acceptCb.call(this, socket);
                }
                catch (Exception ex) /* happened in non-final (overridable) methods */ {
                    ex.printStackTrace();
                }
            }
            stopFuture.complete(null);
            isRunning = false;
        }).start();
        isRunning = true;
    }
    synchronized
    public final Future<Void> stop () {

        if (!isRunning) throw new IllegalStateException();
        toStop = true;
        return stopFuture;
    }
}
