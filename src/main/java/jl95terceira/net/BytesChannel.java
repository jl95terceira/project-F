package jl95terceira.net;

import java.util.function.*;

public class BytesChannel extends GenericChannel<byte[]> {

    public BytesChannel(java.io.InputStream in, java.io.OutputStream out) { super(in, out); }

    @Override protected byte[] toBytes  (byte[] outgoing) { return outgoing; }
    @Override protected byte[] fromBytes(byte[] bytes)  { return bytes; }
}
