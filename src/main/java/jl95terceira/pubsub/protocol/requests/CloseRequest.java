package jl95terceira.pubsub.protocol.requests;

import javax.json.JsonValue;

import jl95terceira.pubsub.JsonSerializable;
import jl95terceira.pubsub.protocol.Request;
import jl95terceira.pubsub.serdes.requests.CloseRequestJsonSerdes;

public class CloseRequest
    extends Request
    implements JsonSerializable {

    @Override public JsonValue toJson() { return CloseRequestJsonSerdes.toJson(this); }
}
