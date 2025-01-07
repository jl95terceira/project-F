package jl95terceira.net;

import jl95terceira.net.util.SocketChannel;

public class SocketStringChannel {

    public static StringChannel get(java.net.Socket socket) throws java.io.IOException {
        return SocketChannel.get(socket, StringChannel::new);
    }
}
