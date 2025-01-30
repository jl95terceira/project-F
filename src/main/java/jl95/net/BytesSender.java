package jl95.net;

public class BytesSender extends Sender<byte[]> {

    public BytesSender(java.io.OutputStream out) { super(out); }

    @Override protected byte[] toBytes  (byte[] outgoing) { return outgoing; }
}
