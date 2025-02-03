package jl95.rpc;

import jl95.rpc.util.SerdesDefaults;

public class StringRequester {

    public static Requester<String, String> get(Requester.Options options) {

        return new Requester<>(options) {

            @Override protected byte[] writeRequest(String object) { return SerdesDefaults.stringToBytes  .call(object); }
            @Override protected String readResponse(byte[] serial) { return SerdesDefaults.stringFromBytes.call(serial); }
        };
    }
}
