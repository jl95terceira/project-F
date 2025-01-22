package jl95terceira.pubsub.serdes.requests;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import jl95terceira.pubsub.protocol.requests.SubscriptionToAll;

public class SubscriptionToAllJsonSerdes {

    public static JsonObject              toJson  (SubscriptionToAll req) {

        return Json.createObjectBuilder().build();
    }
    public static SubscriptionToAll fromJson(JsonValue               reqjson) {

        return new SubscriptionToAll();
    }
}
