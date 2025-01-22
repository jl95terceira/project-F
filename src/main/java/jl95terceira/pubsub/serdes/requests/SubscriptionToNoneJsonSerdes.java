package jl95terceira.pubsub.serdes.requests;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import jl95terceira.pubsub.protocol.requests.SubscriptionToNone;

public class SubscriptionToNoneJsonSerdes {

    public static JsonObject         toJson  (SubscriptionToNone req) {

        return Json.createObjectBuilder().build();
    }
    public static SubscriptionToNone fromJson(JsonValue          reqjson) {

        return new SubscriptionToNone();
    }
}
