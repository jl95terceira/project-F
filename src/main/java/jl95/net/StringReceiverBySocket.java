package jl95.net;

import jl95.net.util.ReceiverBySocket;

public class StringReceiverBySocket {

    public static StringReceiver get(java.net.Socket socket) throws java.io.IOException {
        return ReceiverBySocket.get(socket, StringReceiver::new);
    }
}
