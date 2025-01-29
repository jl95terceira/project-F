package jl95terceira.pubsub;

public class Test {

    public static void main(String[] args) throws Exception {
        var broker = new PubsubServer(new PubsubServer.EditableOptions());
        broker.startAccept();
        Thread.sleep(20000L);
        broker.stopAccept();
    }
}
