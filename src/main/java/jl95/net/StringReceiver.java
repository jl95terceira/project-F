package jl95.net;

import java.io.InputStream;

import jl95.lang.variadic.*;

public class StringReceiver extends Receiver<String> {

    private java.nio.charset.Charset charset;

    public StringReceiver(Function0<InputStream> in) {
        super(in);
        setCharset(java.nio.charset.StandardCharsets.UTF_8);
    }

    public void setCharset(java.nio.charset.Charset charset) {this.charset = charset;}

    @Override protected String fromBytes(byte[] incoming)  { return new String(incoming, charset); }
}
