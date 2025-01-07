package jl95terceira.net.util;

public class SocketChannel {

        public static <T> T get(java.net.Socket     socket,
                                IOStreamFunction<T> constructor) throws java.io.IOException {
        return constructor.apply(socket.getInputStream(), socket.getOutputStream());
    }
}
