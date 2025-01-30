package jl95.net;

public class StringReceiver extends Receiver<String> {

    private java.nio.charset.Charset charset;

    public StringReceiver(java.io.InputStream in) {
        super(in);
        setCharset(java.nio.charset.StandardCharsets.UTF_8);
    }

    public void setCharset(java.nio.charset.Charset charset) {this.charset = charset;}

    @Override protected String fromBytes(byte[] incoming)  { return new String(incoming, charset); }
}
