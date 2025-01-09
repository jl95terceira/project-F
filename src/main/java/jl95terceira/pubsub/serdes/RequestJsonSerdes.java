package jl95terceira.pubsub.serdes;

import static jl95terceira.lang.stt.*;

import java.util.UUID;

import javax.json.*;
import jl95terceira.lang.variadic.*;
import jl95terceira.pubsub.protocol.Request;
import jl95terceira.pubsub.protocol.requests.*;
import jl95terceira.pubsub.serdes.requests.SubscriptionByListRequestJsonSerdes;
import jl95terceira.serdes.ListOfStringToJson;

public class RequestJsonSerdes {

    public enum Id {

        ID  ("id"),
        TYPE("type");

        public final String value;
        Id(String value) {this.value = value;}
    }
    public enum RequestType {
        CLOSE,
        SUBSCRIBE_ALL,
        SUBSCRIBE_NONE,
        SUBSCRIBE_LIST;
    }

    /**
    * serialize extended / sub-classed
    * @param req object to be serialized
    * @param extendedEntries extra entries to be added to the serial
    * @return serial (JSON)
    * @param <T> object type
    */
    public static <T extends Request> JsonObject toJsonExt  (T            req,
                                                             RequestType  type,
                                                             Iterable<Tuple2<String, JsonValue>> extendedEntries) {
        var job = Json.createObjectBuilder();
        for (var t: I(

            tuple(Id.TYPE, Json.createValue(req.id.toString())),
            tuple(Id.ID  , Json.createValue(type  .toString()))

        )) {
            job.add(t.a1.value, t.a2);
        }
        for (var entry: extendedEntries) {
           job.add(entry.a1, entry.a2);
        }
        return job.build();
    }

    /**
     * deserialize
     * @param xjson JSON to be deserialized
     * @return object
     */
    public static                     Request    fromJson   (JsonValue    xjson) {
        return fromJsonExt(xjson, Request::new);
    }

    /**
     * deserialize extended / sub-classed
     * @param xjson JSON to be deserialized
     * @param factory method to obtain a new / pre-populated object which to populate from the serial
     * @return object
     * @param <T> object type
     */
    public static <T extends Request> T          fromJsonExt(JsonValue    xjson,
                                                             Function0<T> factory) {
        var jo = xjson.asJsonObject();
        var x  = factory.call();
        for (var t: I(

            tuple(Id.ID, method((String i) -> x.id = UUID.fromString(jo.getString(i))))

        )) {
            t.a2.call(t.a1.value);
        }
        return x;
    }
}
