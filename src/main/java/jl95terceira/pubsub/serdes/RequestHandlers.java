package jl95terceira.pubsub.serdes;

import jl95terceira.pubsub.protocol.requests.CloseRequest;
import jl95terceira.pubsub.protocol.requests.SubscriptionToAllRequest;
import jl95terceira.pubsub.protocol.requests.SubscriptionToNoneRequest;
import jl95terceira.pubsub.protocol.requests.SubscriptionByListRequest;

public interface RequestHandlers {

    void handle(CloseRequest              request);
    void handle(SubscriptionToAllRequest request);
    void handle(SubscriptionToNoneRequest request);
    void handle(SubscriptionByListRequest request);
}
