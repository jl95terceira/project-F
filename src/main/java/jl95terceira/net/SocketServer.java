package jl95terceira.net;

import static jl95terceira.lang.stt.*;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

import jl95terceira.lang.variadic.Method0;
import jl95terceira.lang.variadic.Method1;
import jl95terceira.net.util.NetDefaults;

public class SocketServer {

    public interface    Options {
        InetSocketAddress  addr           ();
        Integer            acceptTimeoutMs();
        Method1<Socket>    acceptCb       ();
        Method1<Exception> acceptErrorCb  ();
        Method0            acceptTimeoutCb();
    }
    public static class EditableOptions implements Options {

        public InetSocketAddress  addr            = NetDefaults.serverAddr;
        public Integer            acceptTimeoutMs = 1000;
        public Method1<Socket>    acceptCb        = (socket) -> uncheck(() -> {
            System.out.printf(String.format("Got new connection: %s\nConnection callback method is not overridden\nClose connection", socket));
            socket.close();
        });
        public Method1<Exception> acceptErrorCb   = (ex)     -> System.out.printf("Error on accept connection: %s%n", ex);;
        public Method0            acceptTimeoutCb = ()       -> {};

        @Override public InetSocketAddress  addr           () {
        return addr;
        }
        @Override public Integer            acceptTimeoutMs() {
            return acceptTimeoutMs;
        }
        @Override public Method1<Socket>    acceptCb       () {
            return acceptCb;
        }
        @Override public Method1<Exception> acceptErrorCb  () {
            return acceptErrorCb;
        }
        @Override public Method0            acceptTimeoutCb() {
            return acceptTimeoutCb;
        }
    }

    private       java.net.ServerSocket   server;
    private       Boolean                 toStop  = false;
    private final Object                  sync    = new Object();
    private final java.util.List<Method0> stopCbs = new LinkedList<>();

    public final InetSocketAddress    addr;
    public final Integer              acceptTimeoutMs;
    public final Method1<Socket>      acceptCb;
    public final Method1<Exception>   acceptErrorCb;
    public final Method0              acceptTimeoutCb;

    public SocketServer(Options options) {
        this.addr            = options.addr();
        this.acceptTimeoutMs = options.acceptTimeoutMs();
        this.acceptCb        = options.acceptCb       ();
        this.acceptErrorCb   = options.acceptErrorCb  ();
        this.acceptTimeoutCb = options.acceptTimeoutCb();
    }

    public final void start() throws java.io.IOException {
        toStop = false;
        server = new java.net.ServerSocket();
        server.bind(addr);
        server.setSoTimeout(acceptTimeoutMs);
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
    }
}
