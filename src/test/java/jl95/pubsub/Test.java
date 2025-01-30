package jl95.pubsub;

public class Test {

    public static void main(String[] args) throws Exception {
        var broker = new Server(new Server.EditableOptions2());
        broker.startAccept();
        Thread.sleep(20000L);
        broker.stopAccept();
    }
}
