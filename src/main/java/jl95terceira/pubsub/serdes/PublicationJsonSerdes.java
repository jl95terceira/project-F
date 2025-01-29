package jl95terceira.pubsub.serdes;

import static jl95terceira.lang.stt.I;
import static jl95terceira.lang.stt.method;
import static jl95terceira.lang.stt.tuple;

import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import jl95terceira.pubsub.protocol.Publication;
import jl95terceira.pubsub.protocol.requests.SubscriptionByList;
import jl95terceira.pubsub.util.SerdesDefaults;
import jl95terceira.serdes.ListOfStringFromJson;
import jl95terceira.serdes.ListOfStringToJson;
import jl95terceira.serdes.StringToJson;

public class PublicationJsonSerdes {
    public enum Id {

        TOPIC("topic"),
        DATA ("data");

        public final String value;
        Id(String value) {this.value = value;}
    }

    public static JsonObject  toJson  (Publication req) {

        var job = Json.createObjectBuilder();
        for (var t: I(

            tuple(Id.TOPIC, SerdesDefaults.stringToJson.call                                  (req.topic)),
            tuple(Id.DATA , SerdesDefaults.stringToJson.call(SerdesDefaults.bytesToString.call(req.data)))

        ).map(t -> tuple(t.a1.value, t.a2))) {
            job.add(t.a1, t.a2);
        }
        return job.build();
    }
    public static Publication fromJson(JsonValue   reqjson) {

        var jo = reqjson.asJsonObject();
        var x = new Publication();
        for (var t: I(

            tuple(Id.TOPIC, method((String i) -> { x.topic =                                     SerdesDefaults.stringFromJson.call(jo.get(i)); })),
            tuple(Id.DATA , method((String i) -> { x.data  = SerdesDefaults.bytesFromString.call(SerdesDefaults.stringFromJson.call(jo.get(i))); }))

        )) {
                t.a2.call(t.a1.value);
        }
        return x;
    }
}
