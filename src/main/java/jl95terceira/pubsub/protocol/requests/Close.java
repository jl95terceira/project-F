package jl95terceira.pubsub.protocol.requests;

import static jl95terceira.lang.stt.*;

import javax.json.JsonValue;

import jl95terceira.lang.*;
import jl95terceira.lang.variadic.*;
import jl95terceira.pubsub.JsonSerializable;
import jl95terceira.pubsub.serdes.requests.CloseJsonSerdes;

public class Close
    extends NamedDataClass
    implements JsonSerializable {

    @Override public JsonValue toJson() { return CloseJsonSerdes.toJson(this); }

    @Override protected Iterable<Tuple2<String, ?>> namedData() {
        return I();
    }
}
