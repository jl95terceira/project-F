package jl95terceira.pubsub.protocol.requests;

import static jl95terceira.lang.stt.*;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonValue;

import jl95terceira.lang.I;
import jl95terceira.lang.variadic.*;
import jl95terceira.pubsub.JsonSerializable;
import jl95terceira.pubsub.Subscription;
import jl95terceira.pubsub.SubscriptionSupplier;
import jl95terceira.pubsub.protocol.Request;
import jl95terceira.pubsub.serdes.requests.SubscriptionByListRequestJsonSerdes;

public class SubscriptionByListRequest
    extends Request
    implements SubscriptionSupplier, JsonSerializable {

    public enum Action {
        SUBSCRIBE,
        UNSUBSCRIBE;
    }

    public Action       action     = Action.SUBSCRIBE;
    public List<String> topicNames = new ArrayList<>();

    @Override public Subscription getSubscription() {
        var topicNamesSet = I.of(topicNames).toSet();
        return (topic) -> topicNamesSet.contains(topic.name);
    }
    @Override public JsonValue    toJson         () {
        return SubscriptionByListRequestJsonSerdes.toJson(this);
    }

    @Override public Iterable<Tuple2<String,?>> namedData() {
        return I.flat(super.namedData(), I(
            tuple("topicNames", topicNames)
        ));
    }
}
