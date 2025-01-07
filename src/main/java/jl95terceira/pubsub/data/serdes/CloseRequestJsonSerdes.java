package jl95terceira.pubsub.data.serdes;

import static jl95terceira.lang.stt.*;
import javax.json.*;
import jl95terceira.lang.I;
import jl95terceira.lang.variadic.*;
import jl95terceira.pubsub.data.CloseRequest;

public class CloseRequestJsonSerdes {

    public static JsonObject   toJson  (CloseRequest x) {

        return RequestJsonSerdes.toJsonExt(x, I(/* no extra entries*/));
    }
    public static CloseRequest fromJson(JsonValue    xjson) {

    return RequestJsonSerdes.fromJsonExt(xjson, CloseRequest::new);
    }
}
