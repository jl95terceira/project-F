package jl95.net.util;

import java.io.InputStream;

import jl95.lang.variadic.*;

public class ReceiverBySocket {

    public static <T> T get(java.net.Socket           socket,
                            Function1<T, InputStream> constructor) throws java.io.IOException {
        return constructor.call(socket.getInputStream());
    }
}
