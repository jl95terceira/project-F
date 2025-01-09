package jl95terceira.pubsub.serdes.requests;

import static jl95terceira.lang.stt.*;
import javax.json.*;
import jl95terceira.pubsub.protocol.requests.SubscriptionByListRequest;
import jl95terceira.pubsub.serdes.RequestJsonSerdes;
import jl95terceira.serdes.*;

public class SubscriptionByListRequestJsonSerdes {

    public enum Id {

    ACTION     ("action"),
    TOPIC_NAMES("topicNames");

    public final String value;
    Id(String value) {this.value = value;}
    }

    public static JsonObject              toJson  (SubscriptionByListRequest req) {

        return RequestJsonSerdes.toJsonExt(req, RequestJsonSerdes.RequestType.SUBSCRIBE_LIST, I(

            tuple(Id.ACTION     , StringToJson      .get().call(req.action.toString())),
            tuple(Id.TOPIC_NAMES, ListOfStringToJson.get().call(req.topicNames))

        ).map(t -> tuple(t.a1.value, t.a2)));
    }
    public static SubscriptionByListRequest fromJson(JsonValue               reqjson) {

    var jo = reqjson.asJsonObject();
    return RequestJsonSerdes.fromJsonExt(reqjson, () -> {
        var x = new SubscriptionByListRequest();
        for (var t: I(

            tuple(Id.ACTION     , method((String i) -> { x.action     = SubscriptionByListRequest.Action.valueOf(jo.getString(i)); })),
            tuple(Id.TOPIC_NAMES, method((String i) -> { x.topicNames.addAll(ListOfStringFromJson.get().call    (jo.get      (i))); }))

        )) {
            t.a2.call(t.a1.value);
        }
        return x;
    });
    }
}
