package jl95terceira.net;

import java.util.function.*;

public interface Channel<T> {

    void send(T           outgoing);
    void recv(Consumer<T> incomingCb);
}
