package jl95terceira.pubsub;

import static jl95terceira.lang.stt.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import jl95terceira.lang.I;
import jl95terceira.lang.variadic.*;
import jl95terceira.net.*;
import jl95terceira.pubsub.protocol.Message;
import jl95terceira.pubsub.protocol.Publication;
import jl95terceira.pubsub.protocol.requests.Close;
import jl95terceira.pubsub.serdes.PublicationJsonSerdes;
import jl95terceira.pubsub.serdes.SwitchingDeserializer;
import jl95terceira.pubsub.serdes.requests.CloseJsonSerdes;
import jl95terceira.pubsub.serdes.requests.SubscriptionByListJsonSerdes;
import jl95terceira.pubsub.serdes.requests.SubscriptionToAllJsonSerdes;
import jl95terceira.pubsub.serdes.requests.SubscriptionToNoneJsonSerdes;
import jl95terceira.pubsub.util.*;
import jl95terceira.pubsub.util.NetDefaults;
import jl95terceira.serdes.JsonFromString;
import jl95terceira.serdes.StringFromJson;

public class PubsubServer {

    public interface    Options {
        InetSocketAddress  addr           ();
        Integer            acceptTimeoutMs();
        Method1<Exception> acceptErrorCb  ();
        Method0            acceptTimeoutCb();
    }
    public static class EditableOptions implements Options {

        public InetSocketAddress  addr            = NetDefaults.brokerAddr;
        public Integer            acceptTimeoutMs = 1000;
        public Method1<Exception> acceptErrorCb   = (ex)     -> System.out.printf("Error on accept connection: %s%n", ex);;
        public Method0            acceptTimeoutCb = ()       -> {};

        @Override public InetSocketAddress  addr           () {
            return addr;
        }
        @Override public Integer            acceptTimeoutMs() {
            return acceptTimeoutMs;
        }
        @Override public Method1<Exception> acceptErrorCb  () {
            return acceptErrorCb;
        }
        @Override public Method0            acceptTimeoutCb() {
            return acceptTimeoutCb;
        }
    }

    private final Map<InetAddress, Connection> connectionsMap = new ConcurrentHashMap<>();
    private final SocketServer socketServer;

    public PubsubServer(Options options) {
        this.socketServer = new SocketServer(new SocketServer.Options() {

            @Override public InetSocketAddress  addr            () {
                return options.addr();
            }
            @Override public Integer            acceptTimeoutMs () {
                return options.acceptTimeoutMs();
            }
            @Override public Method1<Socket>    acceptCb        () {
                return PubsubServer.this::acceptCb;
            }
            @Override public Method1<Exception> acceptErrorCb   () { return options.acceptErrorCb(); }
            @Override public Method0            acceptTimeoutCb () {
                return options.acceptTimeoutCb();
            }
        });
    }

    private void acceptCb                     (Socket     socket) {
        var connection = new Connection(socket);
        connectionsMap.put(socket.getInetAddress(), connection);
        var switchingDeserializer = new SwitchingDeserializer<Boolean>();
        switchingDeserializer.addCase(
            MessageType.REQ_CLOSE.serial,
            CloseJsonSerdes::fromJson,
            getCloseRequestHandler(connection)
        );
        for (var t: I(
            tuple(MessageType.REQ_SUBSCRIPTION_BY_LIST.serial, function(SubscriptionByListJsonSerdes::fromJson)),
            tuple(MessageType.REQ_SUBSCRIPTION_TO_ALL.serial , function(SubscriptionToAllJsonSerdes ::fromJson)),
            tuple(MessageType.REQ_SUBSCRIPTION_TO_NONE.serial, function(SubscriptionToNoneJsonSerdes::fromJson))
        )) {
            switchingDeserializer.addCase(
                t.a1,
                t.a2,
                getSubscriptionRequestHandler(connection)
            );
        }
        switchingDeserializer.addCase(
            MessageType.PUBLISH.serial,
            PublicationJsonSerdes::fromJson,
            getPublicationHandler(connection)
        );
        var recvOptions = new Receiver.RecvOptions();
        recvOptions.afterStop = () -> {
            uncheck(connection.socket::close);
            connectionsMap.remove(connection.socket.getInetAddress());
        };
        connection.stringReceiver.recvWhile((msg) -> switchingDeserializer.call(SerdesDefaults.jsonFromString.call(msg)), recvOptions);
    }
    private Function1<Boolean, Message<Close>>
                 getCloseRequestHandler       (Connection connection) { return req -> true; }
    private <S extends Subscription> Function1<Boolean, Message<S>>
                 getSubscriptionRequestHandler(Connection connection) {
        return req -> {
            connection.subscription = req.body;
            return false;
        };
    }
    private <S extends Subscription> Function1<Boolean, Message<Publication>>
                 getPublicationHandler        (Connection connection) {
        return req -> {
            return false;
        };
    }
    private void close                        (Connection connection) {
        uncheck(connection.socket::close);
        connectionsMap.remove(connection.socket.getInetAddress());
    }


    public final void                  startAccept     () {
        uncheck(socketServer::start);
    }
    public final void                  stopAccept      () {
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
