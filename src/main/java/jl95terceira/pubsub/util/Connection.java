package jl95terceira.pubsub.util;

import static jl95terceira.lang.stt.*;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import jl95terceira.net.*;
import jl95terceira.pubsub.Subscription;
import jl95terceira.pubsub.protocol.Message;
import jl95terceira.pubsub.protocol.Publication;

public class Connection {

    public final Socket         socket;
    public final BytesSender    sender;
    public final BytesReceiver  receiver;
    public final StringSender   stringSender;
    public final StringReceiver stringReceiver;
    public final Object         queueStartSync = new Object();
    public final BlockingQueue<Message<Publication>>
                                queue          = new ArrayBlockingQueue<>(20);
    public       Subscription   subscription   = (topic) -> false;
    public       Boolean        queueIsOn      = false;
    public       Boolean        queueToStop    = false;

    public Connection(Socket socket) {
        this.socket         = socket;
        this.sender         = uncheck(() -> BytesSenderBySocket   .get(socket));
        this.receiver       = uncheck(() -> BytesReceiverBySocket .get(socket));
        this.stringSender   = uncheck(() -> StringSenderBySocket  .get(socket));
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
