package jl95terceira.net;

import jl95terceira.net.util.ReceiverBySocket;

public class StringReceiverBySocket {

    public static StringReceiver get(java.net.Socket socket) throws java.io.IOException {
        return ReceiverBySocket.get(socket, StringReceiver::new);
    }
}
