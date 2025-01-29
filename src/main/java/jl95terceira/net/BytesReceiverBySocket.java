package jl95terceira.net;

import jl95terceira.net.util.ReceiverBySocket;

public class BytesReceiverBySocket {

    public static BytesReceiver get(java.net.Socket socket) throws java.io.IOException {
        return ReceiverBySocket.get(socket, BytesReceiver::new);
    }
}
