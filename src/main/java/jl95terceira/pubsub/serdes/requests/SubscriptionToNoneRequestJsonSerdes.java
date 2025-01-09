package jl95terceira.pubsub.serdes.requests;

import static jl95terceira.lang.stt.I;

import javax.json.JsonObject;
import javax.json.JsonValue;

import jl95terceira.pubsub.protocol.requests.SubscriptionToNoneRequest;
import jl95terceira.pubsub.serdes.RequestJsonSerdes;

public class SubscriptionToNoneRequestJsonSerdes {

    public static JsonObject                toJson  (SubscriptionToNoneRequest req) {

        return RequestJsonSerdes.toJsonExt(req, RequestJsonSerdes.RequestType.SUBSCRIBE_NONE, I(/* no extra entries*/));
    }
    public static SubscriptionToNoneRequest fromJson(JsonValue                 reqjson) {

        return RequestJsonSerdes.fromJsonExt(reqjson, SubscriptionToNoneRequest::new);
    }
}
