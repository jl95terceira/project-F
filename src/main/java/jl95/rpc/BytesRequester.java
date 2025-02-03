package jl95.rpc;

public class BytesRequester {

    public static Requester<byte[], byte[]> get(Requester.Options options) {

        return new Requester<>(options) {

            @Override protected byte[] writeRequest(byte[] object) {
                return object;
            }
            @Override protected byte[] readResponse(byte[] serial) { return serial; }
        };
    }
}
