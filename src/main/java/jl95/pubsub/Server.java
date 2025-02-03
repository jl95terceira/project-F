package jl95.pubsub;

import static jl95.lang.SuperPowers.*;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import jl95.net.Receiver;
import jl95.pubsub.protocol.requests.Close;
import jl95.pubsub.util.Connection;
import jl95.pubsub.util.MessageType;
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

    public interface Options {
        ServerSocket getSocket      ();
        void         onAcceptError  (Exception ex);
        void         onAcceptTimeout();
    }

    private final Map<InetAddress, Connection> connectionsMap = new ConcurrentHashMap<>();
    private final jl95.net.Server              socketServer;

    public Server(ServerSocket socket,
                  Options      options) {
        this.socketServer = new jl95.net.Server(socket, new jl95.net.Server.Options() {

            @Override public void         onAccept       (jl95.net.Server server, Socket    clientSocket) { Server.this.onAccept(clientSocket); }
            @Override public void         onAcceptError  (jl95.net.Server server, Exception ex) { options.onAcceptError(ex); }
            @Override public void         onAcceptTimeout(jl95.net.Server server) {
                options.onAcceptTimeout();
            }
        });
    }

    private void                                     onAccept          (Socket     socket) {
        var connection = new Connection(socket);
        connectionsMap.put(socket.getInetAddress(), connection);
        var switchingDeserializer = new SwitchingDeserializer<Boolean>();
        switchingDeserializer.addCase(
            MessageType.REQ_CLOSE.serial,
            CloseJsonSerdes::fromJson,
            getCloseReqHandler(connection)
        );
        for (var t: I(
            tuple(MessageType.REQ_SUBSCRIPTION_BY_LIST.serial, function(SubscriptionByListJsonSerdes::fromJson)),
            tuple(MessageType.REQ_SUBSCRIPTION_TO_ALL.serial , function(SubscriptionToAllJsonSerdes ::fromJson)),
            tuple(MessageType.REQ_SUBSCRIPTION_TO_NONE.serial, function(SubscriptionToNoneJsonSerdes::fromJson))
        )) {
            switchingDeserializer.addCase(
                t.a1,
                t.a2,
                getSubReqHandler(connection)
            );
        }
        switchingDeserializer.addCase(
            MessageType.PUBLISH.serial,
            PublicationJsonSerdes::fromJson,
            getPubReqHandler(connection)
        );
        var recvOptions = new Receiver.RecvOptions.Editable<String>();
        recvOptions.afterStop = (receiver) -> {
            uncheck(connection.socket::close);
            connectionsMap.remove(connection.socket.getInetAddress());
        };
        connection.stringReceiver.recvWhile((msg) -> switchingDeserializer.call(SerdesDefaults.jsonFromString.call(msg)), recvOptions);
    }
    private void                                     close             (Connection connection) {
        uncheck(connection.socket::close);
        connectionsMap.remove(connection.socket.getInetAddress());
    }
    private Function1<Boolean, Message<Close>>       getCloseReqHandler(Connection connection) { return req -> true; }
    private <S extends Subscription>
            Function1<Boolean, Message<S>>           getSubReqHandler  (Connection connection) {
        return req -> {
            connection.subscription = req.body;
            return false;
        };
    }
    private <S extends Subscription>
            Function1<Boolean, Message<Publication>> getPubReqHandler  (Connection connection) {
        return req -> {
            return false;
        };
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
    public final Set<InetAddress>      getAddresses    () {
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
