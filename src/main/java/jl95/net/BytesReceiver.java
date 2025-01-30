package jl95.net;

public class BytesReceiver extends Receiver<byte[]> {

    public BytesReceiver(java.io.InputStream in) { super(in); }

    @Override protected byte[] fromBytes(byte[] bytes)  { return bytes; }
}
