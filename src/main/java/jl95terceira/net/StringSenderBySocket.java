package jl95terceira.net;

import jl95terceira.net.util.SenderBySocket;

public class StringSenderBySocket {

    public static StringSender get(java.net.Socket socket) throws java.io.IOException {
        return SenderBySocket.get(socket, StringSender::new);
    }
}
