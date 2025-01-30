package jl95.pubsub.protocol.requests;

import static jl95.lang.SuperPowers.*;

import javax.json.JsonValue;

import jl95.lang.*;
import jl95.lang.variadic.*;
import jl95.pubsub.JsonSerializable;
import jl95.pubsub.serdes.requests.CloseJsonSerdes;

public class Close
    extends NamedDataClass
    implements JsonSerializable {

    @Override public JsonValue toJson() { return CloseJsonSerdes.toJson(this); }

    @Override protected Iterable<Tuple2<String, ?>> namedData() {
        return I();
    }
}
