package jl95.pubsub;

import static jl95.lang.SuperPowers.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import jl95.net.Receiver;
import jl95.net.SocketServer;
import jl95.pubsub.protocol.requests.Close;
import jl95.pubsub.util.Connection;
import jl95.pubsub.util.MessageType;
import jl95.pubsub.util.Defaults;
import jl95.pubsub.util.SerdesDefaults;
import jl95.lang.I;
import jl95.lang.variadic.*;
import jl95.pubsub.protocol.Message;
import jl95.pubsub.protocol.Publication;
import jl95.pubsub.serdes.PublicationJsonSerdes;
import jl95.pubsub.serdes.SwitchingDeserializer;
import jl95.pubsub.serdes.requests.CloseJsonSerdes;
import jl95.pubsub.serdes.requests.SubscriptionByListJsonSerdes;
import jl95.pubsub.serdes.requests.SubscriptionToAllJsonSerdes;
import jl95.pubsub.serdes.requests.SubscriptionToNoneJsonSerdes;

public class Server {

    public interface             Options {
        ServerSocket getSocket      ();
        void         onAcceptError  (Exception ex);
        void         onAcceptTimeout();
    }
    public static abstract class EditableOptionsPartial1 implements Options {

        public Method1<Exception> acceptErrorCb   = (ex) -> System.out.printf("Error on accept connection: %s%n", ex);;
        public Method0            acceptTimeoutCb = ()   -> {};

        @Override public void onAcceptError  (Exception ex) { acceptErrorCb  .call(ex); }
        @Override public void onAcceptTimeout()             { acceptTimeoutCb.call(); }
    }
    public static class          EditableOptions1        extends    EditableOptionsPartial1
                                                         implements Options {

        public Function0<ServerSocket> socketFactory = () -> getSimpleServerSocket(Defaults.brokerAddr, Defaults.brokerAcceptTimeoutMs);

        @Override public ServerSocket getSocket() { return socketFactory.call(); }
    }
    public static class          EditableOptions2        extends    EditableOptionsPartial1
                                                         implements Options {

        public InetSocketAddress  addr            = Defaults.brokerAddr;
        public Integer            acceptTimeoutMs = 1000;

        @Override public ServerSocket getSocket() { return getSimpleServerSocket(addr, acceptTimeoutMs); }
    }

    private static ServerSocket getSimpleServerSocket(InetSocketAddress addr,
                                                      Integer           acceptTimeoutMs) {
        return uncheck(() -> {
            var socket = new ServerSocket();
            socket.bind(addr);
            socket.setSoTimeout(acceptTimeoutMs);
            return socket;
        });
    }

    private final Map<InetAddress, Connection> connectionsMap = new ConcurrentHashMap<>();
    private final SocketServer                 socketServer;

    public Server(Options options) {
        this.socketServer = new SocketServer(new SocketServer.Options() {

            @Override public ServerSocket getServerSocket() {
                return options.getSocket();
            }
            @Override public void         onAccept       (Socket    clientSocket) { acceptCb(clientSocket); }
            @Override public void         onAcceptError  (Exception ex) { options.onAcceptError(ex); }
            @Override public void         onAcceptTimeout() {
                options.onAcceptTimeout();
            }
        });
    }

    private void
    acceptCb                     (Socket     socket) {
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
        var recvOptions = new Receiver.EditableOptions();
        recvOptions.afterStop = () -> {
            uncheck(connection.socket::close);
            connectionsMap.remove(connection.socket.getInetAddress());
        };
        connection.stringReceiver.recvWhile((msg) -> switchingDeserializer.call(SerdesDefaults.jsonFromString.call(msg)), recvOptions);
    }
    private void
    close                        (Connection connection) {
        uncheck(connection.socket::close);
        connectionsMap.remove(connection.socket.getInetAddress());
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

    public final void
    startAccept     () {
        uncheck(socketServer::start);
    }
    public final void
    stopAccept      () {
        uncheck(socketServer::stop);
    }
    public final Iterable<InetAddress>
    getAddressesLazy() {
        return connectionsMap.keySet();
    }
    public final Set<InetAddress>
    getAddresses    () {
            return I.of(getAddressesLazy()).toSet();
    }
    public final Subscription
    getSubscription (InetAddress  iAddr) {
        return connectionsMap.get(iAddr).subscription;
    }
    public final void
    setSubscription (InetAddress  iAddr,
                     Subscription subscription) {
        connectionsMap.get(iAddr).subscription = subscription;
    }
    public final void
    closeAll        () {
        for (var connection: connectionsMap.values()) {
            close(connection);
        }
    }
}
