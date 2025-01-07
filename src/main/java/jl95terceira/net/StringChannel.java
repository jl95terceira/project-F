package jl95terceira.net;

import java.util.function.*;

public class StringChannel extends GenericChannel<String> {

    private java.nio.charset.Charset charset;

    public StringChannel(java.io.InputStream in, java.io.OutputStream out) {
        super(in, out);
        setCharset(java.nio.charset.StandardCharsets.UTF_8);
    }

    public void setCharset(java.nio.charset.Charset charset) {this.charset = charset;}

    @Override protected byte[] toBytes  (String outgoing) { return outgoing.getBytes(charset); }
    @Override protected String fromBytes(byte[] incoming)  { return new String(incoming, charset); }
}
