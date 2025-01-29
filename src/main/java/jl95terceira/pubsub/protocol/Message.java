package jl95terceira.pubsub.protocol;

import java.util.*;

import static jl95terceira.lang.stt.*;
import jl95terceira.lang.*;
import jl95terceira.lang.variadic.Tuple2;

public class Message<B> extends NamedDataClass {

    public UUID id   = UUID.randomUUID();
    public B    body = null;

    @Override protected Iterable<Tuple2<String, ?>> namedData() {
        return I(
            tuple("id"  , id),
            tuple("body", body)
        );
    }
}
