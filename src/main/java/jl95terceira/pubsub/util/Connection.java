package jl95terceira.pubsub.util;

import static jl95terceira.lang.stt.*;

import java.net.Socket;

import jl95terceira.net.*;
import jl95terceira.pubsub.Subscription;

public class Connection {

    public final Socket        socket;
    public final BytesChannel  channel;
    public final StringChannel stringChannel;

    public Subscription subscription = (topic) -> false;

    public Connection(Socket socket) {
        this.socket        = socket;
        this.channel       = uncheck(() -> SocketBytesChannel .get(socket));
        this.stringChannel = uncheck(() -> SocketStringChannel.get(socket));
    }
}
