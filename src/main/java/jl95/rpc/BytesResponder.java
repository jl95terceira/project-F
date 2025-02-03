package jl95.rpc;

public class BytesResponder {

    public static Responder<byte[], byte[]> get(Responder.Options options) {

        return new Responder<>(options) {

            @Override protected byte[] readRequest  (byte[] serial) { return serial; }
            @Override protected byte[] writeResponse(byte[] object) {
                return object;
            }
        };
    }
}
