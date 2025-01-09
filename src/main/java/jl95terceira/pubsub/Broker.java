package jl95terceira.pubsub;

import static jl95terceira.lang.stt.uncheck;

import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import jl95terceira.lang.I;
import jl95terceira.lang.variadic.*;
import jl95terceira.net.*;
import jl95terceira.pubsub.util.Connection;

public class Broker {

    public interface    Options {
        InetAddress        iAddr          ();
        Integer            port           ();
        Integer            acceptTimeoutMs();
        Method1<Exception> acceptErrorCb  ();
        Method0            acceptTimeoutCb();
    }
    public static class EditableOptions {
        public InetAddress        iAddr           = uncheck(() -> java.net.InetAddress.getByAddress(new byte[]{127,0,0,1}));
        public Integer            port            = 4242;
        public Integer            acceptTimeoutMs = 1000;
        public Method1<Exception> acceptErrorCb   = (ex)     -> System.out.printf("Error on accept connection: %s%n", ex);;
        public Method0            acceptTimeoutCb = ()       -> {};
    }

    private final Map<InetAddress, Connection> connectionsMap = new ConcurrentHashMap<>();
    private final SocketServer                 socketServer;

    public Broker(Options         options) {
        this.socketServer = new SocketServer(new SocketServer.Options() {

            @Override public InetAddress        iAddr           () {
                return options.iAddr();
            }
            @Override public Integer            port            () {
                return options.port();
            }
            @Override public Integer            acceptTimeoutMs () {
                return options.acceptTimeoutMs();
            }
            @Override public Method1<Socket>    acceptCb        () {
                return Broker.this::acceptCb;
            }
            @Override public Method1<Exception> acceptErrorCb   () { return options.acceptErrorCb(); }
            @Override public Method0            acceptTimeoutCb () {
                return options.acceptTimeoutCb();
            }
        });
    }
    public Broker(EditableOptions edit) {
        this(new Options() {

            @Override public InetAddress        iAddr          () {
                return edit.iAddr;
            }
            @Override public Integer            port           () {
                return edit.port;
            }
            @Override public Integer            acceptTimeoutMs() {
                return edit.acceptTimeoutMs;
            }
            @Override public Method1<Exception> acceptErrorCb  () {
                return edit.acceptErrorCb;
            }
            @Override public Method0            acceptTimeoutCb() {
                return edit.acceptTimeoutCb;
            }
        });
    }

    private void acceptCb(Socket socket) {
        var connection = new Connection(socket);
        connectionsMap.put(socket.getInetAddress(), connection);
        connection.stringChannel.recv((message) -> {

        });
    }

    public final Iterable<InetAddress> getAddressesLazy() {
        return connectionsMap.keySet();
    }
    public final Iterable<InetAddress> getAddresses    () {
            return I.of(getAddressesLazy()).toSet();
    }
    public final Subscription          getSubscription (InetAddress  iAddr) {
        return connectionsMap.get(iAddr).subscription;
    }
    public final void                  setSubscription (InetAddress  iAddr,
                                                        Subscription subscription) {
        connectionsMap.get(iAddr).subscription = subscription;
    }
}
