package jl95terceira.net;

import jl95terceira.net.util.SenderBySocket;

public class BytesSenderBySocket {

    public static BytesSender get(java.net.Socket socket) throws java.io.IOException {
        return SenderBySocket.get(socket, BytesSender::new);
    }
}
