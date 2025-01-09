package jl95terceira.pubsub.serdes.requests;

import static jl95terceira.lang.stt.I;

import javax.json.JsonObject;
import javax.json.JsonValue;

import jl95terceira.pubsub.protocol.requests.SubscriptionToAllRequest;
import jl95terceira.pubsub.serdes.RequestJsonSerdes;

public class SubscriptionToAllRequestJsonSerdes {

    public static JsonObject              toJson  (SubscriptionToAllRequest req) {

        return RequestJsonSerdes.toJsonExt(req, RequestJsonSerdes.RequestType.SUBSCRIBE_ALL, I(/* no extra entries*/));
    }
    public static SubscriptionToAllRequest fromJson(JsonValue               reqjson) {

        return RequestJsonSerdes.fromJsonExt(reqjson, SubscriptionToAllRequest::new);
    }
}
