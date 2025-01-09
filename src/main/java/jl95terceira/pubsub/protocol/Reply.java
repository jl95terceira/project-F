package jl95terceira.pubsub.protocol;

import static jl95terceira.lang.stt.I;

import jl95terceira.lang.DataClass;

public class Reply extends DataClass {

    public java.util.UUID requestId;

    @Override
    protected Iterable<?> data() {
        return I(requestId);
    }
}
