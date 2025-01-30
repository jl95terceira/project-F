package jl95.pubsub.util;

import static jl95.lang.SuperPowers.*;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import jl95.net.BytesReceiver;
import jl95.net.BytesReceiverBySocket;
import jl95.net.BytesSender;
import jl95.net.BytesSenderBySocket;
import jl95.net.StringReceiver;
import jl95.net.StringReceiverBySocket;
import jl95.net.StringSender;
import jl95.net.StringSenderBySocket;
import jl95.pubsub.protocol.Message;
import jl95.net.*;
import jl95.pubsub.Subscription;
import jl95.pubsub.protocol.Publication;

public class Connection {

    public final Socket         socket;
    public final BytesSender sender;
    public final BytesReceiver receiver;
    public final StringSender stringSender;
    public final StringReceiver stringReceiver;
    public final Object         queueStartSync = new Object();
    public final BlockingQueue<Message<Publication>>
                                queue          = new ArrayBlockingQueue<>(20);
    public       Subscription   subscription   = (topic) -> false;
    public       Boolean        queueIsOn      = false;
    public       Boolean        queueToStop    = false;

    public Connection(Socket socket) {
        this.socket         = socket;
        this.sender         = uncheck(() -> BytesSenderBySocket.get(socket));
        this.receiver       = uncheck(() -> BytesReceiverBySocket.get(socket));
        this.stringSender   = uncheck(() -> StringSenderBySocket.get(socket));
        this.stringReceiver = uncheck(() -> StringReceiverBySocket.get(socket));
    }

    public void startQueueThread() {
        synchronized (queueStartSync) {
            if (queueIsOn) return;
            queueIsOn = true;
        }
        new Thread(() -> {
            while (!queueToStop) {
                var pub = queue.remove();
                stringSender.send(SerdesDefaults.jsonToString.call(SerdesDefaults.publicationMessageToJson.call(pub)));
            }
        }).start();
    }
}
