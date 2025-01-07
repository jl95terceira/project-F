package jl95terceira.net;

import jl95terceira.net.util.SocketChannel;

public class SocketBytesChannel {

    public static BytesChannel get(java.net.Socket socket) throws java.io.IOException {
        return SocketChannel.get(socket, BytesChannel::new);
    }
}
