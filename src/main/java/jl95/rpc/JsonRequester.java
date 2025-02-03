package jl95.rpc;

import javax.json.JsonValue;

import jl95.rpc.util.SerdesDefaults;

public class JsonRequester {

    public static Requester<JsonValue, JsonValue> get(Requester.Options options) {

        return new Requester<>(options) {

            @Override protected byte[]    writeRequest(JsonValue object) { return SerdesDefaults.jsonToBytes  .call(object); }
            @Override protected JsonValue readResponse(byte[]    serial) { return SerdesDefaults.jsonFromBytes.call(serial); }
        };
    }
}
