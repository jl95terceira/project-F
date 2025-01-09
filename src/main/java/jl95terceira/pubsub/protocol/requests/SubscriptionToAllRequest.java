package jl95terceira.pubsub.protocol.requests;

import javax.json.JsonValue;

import jl95terceira.pubsub.JsonSerializable;
import jl95terceira.pubsub.Subscription;
import jl95terceira.pubsub.SubscriptionSupplier;
import jl95terceira.pubsub.protocol.Request;
import jl95terceira.pubsub.serdes.requests.SubscriptionToAllRequestJsonSerdes;

public class SubscriptionToAllRequest
    extends Request
    implements SubscriptionSupplier, JsonSerializable {

    @Override public Subscription getSubscription() { return (topic) -> true; }
    @Override public JsonValue    toJson         () {
        return SubscriptionToAllRequestJsonSerdes.toJson(this);
    }
}
