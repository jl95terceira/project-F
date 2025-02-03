package jl95.net;

import java.io.OutputStream;

import jl95.lang.variadic.*;

public class StringSender extends Sender<String> {

    private java.nio.charset.Charset charset;

    public StringSender(Function0<OutputStream> out) {
        super(out);
        setCharset(java.nio.charset.StandardCharsets.UTF_8);
    }

    public void setCharset(java.nio.charset.Charset charset) {this.charset = charset;}

    @Override protected byte[] toBytes  (String outgoing) { return outgoing.getBytes(charset); }
}
