package jl95.pubsub;

import java.net.InetSocketAddress;
import java.net.ServerSocket;

import jl95.net.util.Util;
import jl95.pubsub.util.Defaults;
import jl95.pubsub.util.ServerEditableOptionsPartial;

public class ServerEditableOptions2 extends ServerEditableOptionsPartial implements Server.Options {

    public InetSocketAddress addr = Defaults.brokerAddr;
    public Integer acceptTimeoutMs = 1000;

    @Override
    public ServerSocket getSocket() {
        return Util.getSimpleServerSocket(addr, acceptTimeoutMs);
    }
}
