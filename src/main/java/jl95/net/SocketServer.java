package jl95.net;

import static jl95.lang.SuperPowers.*;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import jl95.net.util.Defaults;
import jl95.lang.variadic.*;

public class SocketServer {

    public interface    Options {
        ServerSocket getServerSocket();
        void         onAccept       (Socket    clientSocket);
        void         onAcceptError  (Exception ex);
        void         onAcceptTimeout();
    }
    public static class EditableOptions implements Options {

        public InetSocketAddress  addr            = Defaults.serverAddr;
        public Integer            acceptTimeoutMs = 1000;
        public Method1<Socket>    acceptCb        = (socket) -> uncheck(() -> {
            System.out.printf(String.format("Got new connection: %s\nConnection callback method is not overridden\nClose connection", socket));
            socket.close();
        });
        public Method1<Exception> acceptErrorCb   = (ex)     -> System.out.printf("Error on accept connection: %s%n", ex);;
        public Method0            acceptTimeoutCb = ()       -> {};

        @Override public ServerSocket getServerSocket() { return uncheck(() -> {
            var socket = new ServerSocket();
            socket.bind(addr);
            socket.setSoTimeout(acceptTimeoutMs);
            return socket;
        }); }
        @Override public void         onAccept       (Socket    clientSocket) { acceptCb.call(clientSocket); }
        @Override public void         onAcceptError  (Exception ex) {
            acceptErrorCb.call(ex);
        }
        @Override public void         onAcceptTimeout() {
            acceptTimeoutCb.call();
        }
    }

    private       java.net.ServerSocket   server;
    private       Boolean                 toStop  = false;
    private final Object                  sync    = new Object();
    private final java.util.List<Method0> stopCbs = new LinkedList<>();

    public final Function0<ServerSocket> socketFactory;
    public final Method1<Socket>         acceptCb;
    public final Method1<Exception>      acceptErrorCb;
    public final Method0                 acceptTimeoutCb;

    public SocketServer(Options options) {
        this.socketFactory   = options::getServerSocket;
        this.acceptCb        = options::onAccept;
        this.acceptErrorCb   = options::onAcceptError;
        this.acceptTimeoutCb = options::onAcceptTimeout;
    }

    public final void start() throws java.io.IOException {
        toStop = false;
        server = socketFactory.call();
        new Thread(() -> {
            while (!toStop) {
                try {
                    java.net.Socket socket;
                    try {
                        synchronized (sync) {
                            socket = server.accept();
                        }
                    }
                    catch (java.net.SocketTimeoutException ex) /* not really an error - just to give control back to the thread every so often */ {
                        acceptTimeoutCb.call();
                        continue;
                    }
                    catch (Exception ex) {
                        acceptErrorCb.call(ex);
                        continue;
                    }
                    stopCbs.add(unchecked(() -> {
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                    }));
                    acceptCb.call(socket);
                }
                catch (Exception ex) /* happened in non-final (overridable) methods */ {
                    ex.printStackTrace();
                }
            }
        }).start();
    }
    public final void stop () throws java.io.IOException {
        toStop = true;
        synchronized (sync) {/* wait */}
        for (var stopCb: stopCbs) stopCb.call();
        server.close();
        server = null; // socket is now garbage - collect
    }
}
