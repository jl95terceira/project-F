package jl95.net;

import jl95.net.util.SenderBySocket;

public class StringSenderBySocket {

    public static StringSender get(java.net.Socket socket) throws java.io.IOException {
        return SenderBySocket.get(socket, StringSender::new);
    }
}
