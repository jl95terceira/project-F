package jl95.net.util;

import java.io.OutputStream;

import jl95.lang.variadic.*;

public class SenderBySocket {

    public static <T> T get(java.net.Socket            socket,
                            Function1<T, OutputStream> constructor) throws java.io.IOException {
        return constructor.call(socket.getOutputStream());
    }
}
