package jl95.pubsub.protocol.requests;

import static jl95.lang.SuperPowers.*;

import javax.json.JsonValue;

import jl95.pubsub.JsonSerializable;
import jl95.pubsub.Subscription;
import jl95.pubsub.Topic;
import jl95.lang.*;
import jl95.lang.variadic.*;
import jl95.pubsub.*;
import jl95.pubsub.serdes.requests.SubscriptionToNoneJsonSerdes;

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
