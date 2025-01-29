package jl95terceira.net;

import static jl95terceira.lang.stt.uncheck;

import jl95terceira.lang.variadic.Function1;
import jl95terceira.lang.variadic.Method0;
import jl95terceira.lang.variadic.Method1;

public abstract class Sender<T> {

    private final java.io.OutputStream out;

    public Sender(java.io.OutputStream out) {
        this.out     = out;
    }

    public final void send(T outgoing) {
        var outgoingAsBytes = toBytes(outgoing);
        var size            = outgoingAsBytes.length;
        var sizeAsBytes     = java.math.BigInteger.valueOf(size).toByteArray();
        uncheck(() -> {
        out.write(sizeAsBytes.length);
        out.write(sizeAsBytes);
        out.write(outgoingAsBytes);
    });
    }

    protected abstract byte[] toBytes(T outgoing);
}