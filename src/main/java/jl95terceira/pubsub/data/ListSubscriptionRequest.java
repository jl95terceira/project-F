package jl95terceira.pubsub.data;

import static jl95terceira.lang.stt.*;
import java.util.ArrayList;
import java.util.List;
import jl95terceira.lang.I;
import jl95terceira.lang.variadic.*;

public class ListSubscriptionRequest extends Request {

    public List<String> topicNames = new ArrayList<>();

    public ListSubscriptionRequest() {}
    public ListSubscriptionRequest(String... topicNames) {
        this(I(topicNames));
    }
    public ListSubscriptionRequest(Iterable<String> topicNames) {
        for (var topicName: topicNames) {
            this.topicNames.add(topicName);
        }
    }

    @Override
    public Iterable<Tuple2<String,?>> namedData() {
        return I.flat(super.namedData(), I(
            tuple("topicNames", topicNames)
        ));
    }
}
