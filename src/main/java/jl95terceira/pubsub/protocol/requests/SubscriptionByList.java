package jl95terceira.pubsub.protocol.requests;

import static jl95terceira.lang.stt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.json.JsonValue;

import jl95terceira.lang.I;
import jl95terceira.lang.NamedDataClass;
import jl95terceira.lang.variadic.*;
import jl95terceira.pubsub.JsonSerializable;
import jl95terceira.pubsub.Subscription;
import jl95terceira.pubsub.SubscriptionSupplier;
import jl95terceira.pubsub.Topic;
import jl95terceira.pubsub.serdes.requests.SubscriptionByListJsonSerdes;

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
