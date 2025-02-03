package jl95.pubsub;

import jl95.net.util.Util;
import jl95.pubsub.util.Defaults;

public class Test {

    public static void main(String[] args) throws Exception {
        var broker = new Server(Util.getSimpleServerSocket(Defaults.brokerAddr, Defaults.brokerAcceptTimeoutMs), new ServerEditableOptions2());
        broker.startAccept();
        Thread.sleep(20000L);
        broker.stopAccept();
    }
}
