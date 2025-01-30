package jl95.net;

import jl95.net.util.ReceiverBySocket;

public class BytesReceiverBySocket {

    public static BytesReceiver get(java.net.Socket socket) throws java.io.IOException {
        return ReceiverBySocket.get(socket, BytesReceiver::new);
    }
}
