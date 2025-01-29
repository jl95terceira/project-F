package jl95terceira.net.util;

import java.io.InputStream;

import jl95terceira.lang.variadic.Function1;

public class ReceiverBySocket {

    public static <T> T get(java.net.Socket           socket,
                            Function1<T, InputStream> constructor) throws java.io.IOException {
        return constructor.call(socket.getInputStream());
    }
}
