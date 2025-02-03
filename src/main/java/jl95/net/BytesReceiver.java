package jl95.net;

import java.io.InputStream;

import jl95.lang.variadic.*;

public class BytesReceiver extends Receiver<byte[]> {

    public BytesReceiver(Function0<InputStream> inputGetter) { super(inputGetter); }

    @Override protected byte[] fromBytes(byte[] bytes)  { return bytes; }
}
