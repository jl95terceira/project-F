package jl95.pubsub.protocol.requests;

import static jl95.lang.SuperPowers.*;
import java.util.HashSet;
import java.util.Set;

import javax.json.JsonValue;

import jl95.lang.NamedDataClass;
import jl95.lang.variadic.*;
import jl95.pubsub.JsonSerializable;
import jl95.pubsub.Subscription;
import jl95.pubsub.Topic;
import jl95.pubsub.serdes.requests.SubscriptionByListJsonSerdes;

public class SubscriptionByList
    extends NamedDataClass
    implements Subscription, JsonSerializable {

    public enum Action {
        SUBSCRIBE,
        UNSUBSCRIBE;
    }

    public Action      action     = Action.SUBSCRIBE;
    public Set<String> topicNames = new HashSet<>();

    @Override public Boolean   isInSubscription(Topic topic) {
        return topicNames.contains(topic.name);
    }
    @Override public JsonValue toJson          () {
        return SubscriptionByListJsonSerdes.toJson(this);
    }

    @Override public Iterable<Tuple2<String,?>> namedData() {
        return I(
            tuple("action"    , action),
            tuple("topicNames", topicNames)
        );
    }
}
