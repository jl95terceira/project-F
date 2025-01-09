package jl95terceira.pubsub;

import static jl95terceira.lang.stt.*;
import jl95terceira.lang.NamedDataClass;
import jl95terceira.lang.variadic.Tuple2;

public class Topic extends NamedDataClass {

    public String name;

    @Override protected Iterable<Tuple2<String, ?>> namedData() {
        return I(
            tuple("name", name)
        );
    }
}
