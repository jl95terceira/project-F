package jl95terceira.pubsub.protocol;

import static jl95terceira.lang.stt.*;

import jl95terceira.lang.*;
import jl95terceira.lang.variadic.*;

public class Publication extends NamedDataClass {

    public String topic;
    public byte[] data;

    @Override
    protected Iterable<Tuple2<String, ?>> namedData() {
        return I(
            tuple("topic", topic),
            tuple("data" , data)
        );
    }
}
