package jl95terceira.pubsub;

import static jl95terceira.lang.stt.*;

import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import jl95terceira.lang.I;
import jl95terceira.lang.variadic.*;
import jl95terceira.net.*;
import jl95terceira.pubsub.protocol.Request;
import jl95terceira.pubsub.protocol.requests.Close;
import jl95terceira.pubsub.protocol.requests.SubscriptionByList;
import jl95terceira.pubsub.protocol.requests.SubscriptionToAll;
import jl95terceira.pubsub.protocol.requests.SubscriptionToNone;
import jl95terceira.pubsub.serdes.SwitchingDeserializer;
import jl95terceira.pubsub.serdes.requests.CloseJsonSerdes;
import jl95terceira.pubsub.serdes.requests.SubscriptionByListJsonSerdes;
import jl95terceira.pubsub.serdes.requests.SubscriptionToAllJsonSerdes;
import jl95terceira.pubsub.serdes.requests.SubscriptionToNoneJsonSerdes;
import jl95terceira.pubsub.util.*;
import jl95terceira.serdes.JsonFromString;

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

    private final Map<InetAddress, Connection> connectionsMap        = new ConcurrentHashMap<>();
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

    private void acceptCb                     (Socket     socket) {
        var connection = new Connection(socket);
        connectionsMap.put(socket.getInetAddress(), connection);
        var switchingDeserializer = new SwitchingDeserializer<Boolean>();
        switchingDeserializer.addCase(
            RequestType.CLOSE.serial,
            CloseJsonSerdes::fromJson,
            getCloseRequestHandler(connection)
        );
        for (var t: I(
            tuple(RequestType.SUBSCRIPTION_BY_LIST, function(SubscriptionByListJsonSerdes::fromJson)),
            tuple(RequestType.SUBSCRIPTION_TO_ALL , function(SubscriptionToAllJsonSerdes ::fromJson)),
            tuple(RequestType.SUBSCRIPTION_TO_NONE, function(SubscriptionToNoneJsonSerdes::fromJson))
        )) {
            switchingDeserializer.addCase(
                t.a1.serial,
                t.a2,
                getSubscriptionRequestHandler(connection)
            );
        }
        var recvOptions = new GenericChannel.RecvOptions();
        recvOptions.afterStop = () -> {
            uncheck(connection.socket::close);
            connectionsMap.remove(connection.socket.getInetAddress());
        };
        connection.stringChannel.recvWhile((msg) -> switchingDeserializer.call(JsonFromString.get().call(msg)), recvOptions);
    }
    private Function1<Boolean, Request<Close>>
                 getCloseRequestHandler       (Connection connection) { return req -> true; }
    private <S extends Subscription> Function1<Boolean, Request<S>>
                 getSubscriptionRequestHandler(Connection connection) {
        return req -> {
            connection.subscription = req.body;
            return false;
        };
    }
    private void close                        (Connection connection) {
        uncheck(connection.socket::close);
        connectionsMap.remove(connection.socket.getInetAddress());
    }


    public final void                  start           () {
        uncheck(socketServer::start);
    }
    public final void                  stop            () {
        uncheck(socketServer::stop);
    }
    public final Iterable<InetAddress> getAddressesLazy() {
        return connectionsMap.keySet();
    }
    public final Set     <InetAddress> getAddresses    () {
            return I.of(getAddressesLazy()).toSet();
    }
    public final Subscription          getSubscription (InetAddress  iAddr) {
        return connectionsMap.get(iAddr).subscription;
    }
    public final void                  setSubscription (InetAddress  iAddr,
                                                        Subscription subscription) {
        connectionsMap.get(iAddr).subscription = subscription;
    }
    public final void                  closeAll        () {
        for (var connection: connectionsMap.values()) {
            close(connection);
        }
    }
}
