package jl95terceira.pubsub.serdes.requests;

import static jl95terceira.lang.stt.*;
import javax.json.*;
import jl95terceira.pubsub.protocol.requests.SubscriptionByList;
import jl95terceira.serdes.*;

public class SubscriptionByListJsonSerdes {

    public enum Id {

    ACTION     ("action"),
    TOPIC_NAMES("topicNames");

    public final String value;
    Id(String value) {this.value = value;}
    }

    public static JsonObject         toJson  (SubscriptionByList req) {

        var job = Json.createObjectBuilder();
        for (var t: I(

            tuple(Id.ACTION     , StringToJson      .get().call(req.action.toString())),
            tuple(Id.TOPIC_NAMES, ListOfStringToJson.get().call(req.topicNames))

        ).map(t -> tuple(t.a1.value, t.a2))) {
            job.add(t.a1, t.a2);
        }
        return job.build();
    }
    public static SubscriptionByList fromJson(JsonValue          reqjson) {

        var jo = reqjson.asJsonObject();
        var x = new SubscriptionByList();
        for (var t: I(

            tuple(Id.ACTION     , method((String i) -> { x.action     = SubscriptionByList.Action.valueOf   (jo.getString(i)); })),
            tuple(Id.TOPIC_NAMES, method((String i) -> { x.topicNames.addAll(ListOfStringFromJson.get().call(jo.get      (i))); }))

        )) {
                t.a2.call(t.a1.value);
        }
        return x;
    }
}
