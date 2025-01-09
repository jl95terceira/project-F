package jl95terceira.pubsub.serdes.requests;

import static jl95terceira.lang.stt.*;
import javax.json.*;

import jl95terceira.pubsub.protocol.requests.CloseRequest;
import jl95terceira.pubsub.serdes.RequestJsonSerdes;

public class CloseRequestJsonSerdes {

    public static JsonObject   toJson  (CloseRequest req) {

        return RequestJsonSerdes.toJsonExt(req, RequestJsonSerdes.RequestType.CLOSE, I());
    }
    public static CloseRequest fromJson(JsonValue    reqjson) {

        return RequestJsonSerdes.fromJsonExt(reqjson, CloseRequest::new);
    }
}
