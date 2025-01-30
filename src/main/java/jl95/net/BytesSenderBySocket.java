package jl95.net;

import jl95.net.util.SenderBySocket;

public class BytesSenderBySocket {

    public static BytesSender get(java.net.Socket socket) throws java.io.IOException {
        return SenderBySocket.get(socket, BytesSender::new);
    }
}
