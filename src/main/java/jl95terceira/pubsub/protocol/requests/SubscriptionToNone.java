package jl95terceira.pubsub.protocol.requests;

import static jl95terceira.lang.stt.*;

import javax.json.JsonValue;

import jl95terceira.lang.*;
import jl95terceira.lang.variadic.*;
import jl95terceira.pubsub.*;
import jl95terceira.pubsub.serdes.requests.SubscriptionToNoneJsonSerdes;

public class SubscriptionToNone
    extends NamedDataClass
    implements Subscription, JsonSerializable {

    @Override public Boolean   isInSubscription(Topic topic) {
        return false;
    }
    @Override public JsonValue toJson          () {
        return SubscriptionToNoneJsonSerdes.toJson(this);
    }
    @Override protected Iterable<Tuple2<String, ?>> namedData() {
        return I();
    }
}
