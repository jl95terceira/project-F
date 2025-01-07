package jl95terceira.pubsub.data.serdes;

import static jl95terceira.lang.stt.*;
import javax.json.*;
import jl95terceira.pubsub.data.ListSubscriptionRequest;
import jl95terceira.serdes.*;

public class ListSubscriptionRequestJsonSerdes {

    public enum Id {

    TOPIC_NAMES("topicNames");

    public final String value;
    Id(String value) {this.value = value;}
    }

    public static JsonObject              toJson  (ListSubscriptionRequest x) {

        return RequestJsonSerdes.toJsonExt(x, I(
            tuple(Id.TOPIC_NAMES, ListOfStringToJson.get().call(x.topicNames))
        ).map(t -> tuple(t.a1.value, t.a2)));
    }
    public static ListSubscriptionRequest fromJson(JsonValue               xjson) {

    var jo = xjson.asJsonObject();
    return RequestJsonSerdes.fromJsonExt(xjson, () -> {
        var x = new ListSubscriptionRequest();
        for (var t: I(
            tuple(Id.TOPIC_NAMES, method((String i) -> { x.topicNames.addAll(ListOfStringFromJson.get().call(jo.get(i))); }))
        )) {
            t.a2.call(t.a1.value);
        }
        return x;
    });
    }
}
