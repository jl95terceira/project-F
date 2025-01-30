package jl95.pubsub.protocol.requests;

import static jl95.lang.SuperPowers.*;

import javax.json.JsonValue;

import jl95.pubsub.JsonSerializable;
import jl95.pubsub.Subscription;
import jl95.pubsub.Topic;
import jl95.pubsub.serdes.requests.SubscriptionToAllJsonSerdes;
import jl95.lang.*;
import jl95.lang.variadic.*;
import jl95.pubsub.*;

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
