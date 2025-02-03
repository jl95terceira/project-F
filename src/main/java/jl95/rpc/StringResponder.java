package jl95.rpc;

import jl95.rpc.util.SerdesDefaults;

public class StringResponder {

    public static Responder<String, String> get(Responder.Options options) {

        return new Responder<>(options) {

            @Override protected String readRequest  (byte[] serial) { return SerdesDefaults.stringFromBytes.call(serial); }
            @Override protected byte[] writeResponse(String object) { return SerdesDefaults.stringToBytes  .call(object); }
        };
    }
}
