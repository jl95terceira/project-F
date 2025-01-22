package jl95terceira.pubsub.protocol.requests;

import static jl95terceira.lang.stt.I;

import javax.json.JsonValue;

import jl95terceira.lang.*;
import jl95terceira.lang.variadic.*;
import jl95terceira.pubsub.*;
import jl95terceira.pubsub.serdes.requests.SubscriptionToAllJsonSerdes;

public class SubscriptionToAll
    extends NamedDataClass
    implements Subscription, JsonSerializable {

    @Override public    Boolean      isInSubscription(Topic topic) {
        return true;
    }
    @Override public    JsonValue    toJson          () {
        return SubscriptionToAllJsonSerdes.toJson(this);
    }
    @Override protected Iterable<Tuple2<String, ?>> namedData() {
        return I();
    }

}
