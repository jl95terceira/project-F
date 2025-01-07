package jl95terceira.pubsub;

import static jl95terceira.lang.stt.*;

import java.util.ArrayList;
import java.util.LinkedList;

import jl95terceira.lang.variadic.*;
import jl95terceira.net.*;

public class Broker {

    private       java.net.ServerSocket   server;
    private       Boolean                 toStop  = false;
    private final Object                  sync    = new Object();
    private final java.util.List<Method0> stopCbs = new LinkedList<>();

    public final java.net.InetAddress iAddr;
    public final Integer              port;
    public final Integer              acceptTimeoutMs;
    public final Handlers             handlers;

    public static class Handlers {
        public Method0            acceptTimeout;
        public Method1<Exception> acceptError;
    }

    public static class Options {
        public java.net.InetAddress iAddr           = uncheck(() -> java.net.InetAddress.getByAddress(new byte[]{127,0,0,1}));
        public Integer              port            = 4242;
        public Integer              acceptTimeoutMs = 1000;
        public Handlers             handlers        = new Handlers();
    }

    public Broker(Options              options) {
        this.iAddr           = options.iAddr;
        this.port            = options.port;
        this.acceptTimeoutMs = options.acceptTimeoutMs;
        this.handlers        = options.handlers;
    }

    private void onNewConnection(java.net.Socket socket) {

        uncheck(() -> SocketBytesChannel.get(socket)).recv(payload -> {

            System.out.printf("Broker recv NOT implemented - got: %s%n", payload.length);

        });
    }

    public void start() throws java.io.IOException {
        toStop = false;
        server = new java.net.ServerSocket();
        server.bind(new java.net.InetSocketAddress(iAddr, port));
        server.setSoTimeout(acceptTimeoutMs);
        new Thread(() -> {
            while (!toStop) {
                java.net.Socket socket;
                try {
                    synchronized (sync) {
                        socket = server.accept();
                    }
                }
                catch (java.net.SocketTimeoutException ex) /* not really an error - just to give control back to the thread every so often */ {
                    ifNull(handlers.acceptTimeout, () -> {}).call();
                    continue;
                }
                catch (Exception ex) {
                    ifNull(handlers.acceptError, ex_ -> System.out.printf("Error on accept connection: %s%n", ex_)).call(ex);
                    continue;
                }
                stopCbs.add(unchecked(socket::close));
                onNewConnection(socket);
            }
        }).start();
    }
    public void stop () throws java.io.IOException {
        toStop = true;
        synchronized (sync) {/* wait */}
        for (var stopCb: stopCbs) stopCb.call();
        server.close();
    }
}
