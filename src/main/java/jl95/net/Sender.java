package jl95.net;

import static jl95.lang.SuperPowers.*;

import jl95.lang.variadic.*;

public abstract class Sender<T> {

    private final java.io.OutputStream output;

    protected abstract byte[] toBytes(T outgoing);

    public Sender(java.io.OutputStream output) {
        this.output = output;
    }

    public final void send(T outgoing) {
        var outgoingAsBytes = toBytes(outgoing);
        var size            = outgoingAsBytes.length;
        var sizeAsBytes     = java.math.BigInteger.valueOf(size).toByteArray();
        uncheck(() -> {
        output.write(sizeAsBytes.length);
        output.write(sizeAsBytes);
        output.write(outgoingAsBytes);
    });
    }
    public final <T2> Sender<T2> extend(Function1<T, T2> adapterFunction) {
        return new Sender<T2>(output) {

            @Override protected byte[] toBytes(T2 incoming) {
                return Sender.this.toBytes(adapterFunction.call(incoming));
            }
        };
    }
}