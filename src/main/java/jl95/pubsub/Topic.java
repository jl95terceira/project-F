package jl95.pubsub;

import static jl95.lang.SuperPowers.*;
import jl95.lang.NamedDataClass;
import jl95.lang.variadic.Tuple2;

public class Topic extends NamedDataClass {

    public String name;

    @Override protected Iterable<Tuple2<String, ?>> namedData() {
        return I(
            tuple("name", name)
        );
    }
}
