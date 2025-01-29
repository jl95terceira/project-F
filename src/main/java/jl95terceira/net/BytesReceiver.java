package jl95terceira.net;

public class BytesReceiver extends Receiver<byte[]> {

    public BytesReceiver(java.io.InputStream in) { super(in); }

    @Override protected byte[] fromBytes(byte[] bytes)  { return bytes; }
}
