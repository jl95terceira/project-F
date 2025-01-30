package jl95.pubsub.serdes;

import static jl95.lang.SuperPowers.*;

import java.util.Map;
import java.util.UUID;

import javax.json.JsonValue;

import jl95.pubsub.protocol.Message;
import jl95.pubsub.util.SerdesDefaults;
import jl95.lang.SuperPowers.*;
import jl95.lang.variadic.Function1;

public class SwitchingDeserializer<R> implements Function1<R, JsonValue> {

    private Map<String, Function1<R, JsonValue>> callbacksMap = Map();

    public final <B> void addCase(String type, Function1<B, JsonValue> bodyDeserializer, Function1<R, Message<B>> callback) {
        callbacksMap.put(type, json -> {

            var jsonO = json.asJsonObject();
            var req = new Message<B>();
            for (var t : I(tuple(Serializer.Id.ID, method((String i) -> {
                req.id = UUID.fromString(SerdesDefaults.stringFromJson.call(jsonO.get(i)));
            })), tuple(Serializer.Id.BODY, method((String i) -> {
                req.body = bodyDeserializer.call(jsonO.get(i));
            })))) {
                t.a2.call(t.a1.value);
            }
            return callback.call(req);
        });
    }

    @Override
    public R call(JsonValue json) {
        return callbacksMap.get(SerdesDefaults.stringFromJson.call(json.asJsonObject().get(Serializer.Id.TYPE.value))).call(json);
    }
}
