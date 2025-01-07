package jl95terceira.pubsub.data;

import static jl95terceira.lang.stt.*;
import jl95terceira.lang.*;
import jl95terceira.lang.variadic.Tuple2;

public class Request extends NamedDataClass {

    public java.util.UUID id = java.util.UUID.randomUUID();

    @Override
    protected Iterable<Tuple2<String, ?>> namedData() {
        return I(
            tuple("id", id)
        );
    }
}
