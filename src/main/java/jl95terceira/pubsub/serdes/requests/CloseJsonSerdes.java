package jl95terceira.pubsub.serdes.requests;

import javax.json.*;

import jl95terceira.pubsub.protocol.requests.Close;

public class CloseJsonSerdes {

    public static JsonObject   toJson  (Close req) {

        return Json.createObjectBuilder().build();
    }
    public static Close fromJson(JsonValue    reqjson) {

        return new Close();
    }
}
