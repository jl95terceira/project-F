package jl95terceira.pubsub.data.serdes;

import static jl95terceira.lang.stt.I;

import javax.json.JsonObject;
import javax.json.JsonValue;

import jl95terceira.pubsub.data.FullSubscriptionRequest;

public class FullSubscriptionRequestJsonSerdes {

    public static JsonObject              toJson  (FullSubscriptionRequest x) {

        return RequestJsonSerdes.toJsonExt(x, I(/* no extra entries*/));
    }
    public static FullSubscriptionRequest fromJson(JsonValue               xjson) {

    return RequestJsonSerdes.fromJsonExt(xjson, FullSubscriptionRequest::new);
    }
}
