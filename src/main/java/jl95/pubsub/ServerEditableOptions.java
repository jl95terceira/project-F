package jl95.pubsub;

import java.net.ServerSocket;

import jl95.lang.variadic.Function0;
import jl95.net.util.Util;
import jl95.pubsub.util.Defaults;
import jl95.pubsub.util.ServerEditableOptionsPartial;

public class ServerEditableOptions extends ServerEditableOptionsPartial implements Server.Options {

    public Function0<ServerSocket> socketFactory = () -> Util.getSimpleServerSocket(Defaults.brokerAddr, Defaults.brokerAcceptTimeoutMs);

    @Override
    public ServerSocket getSocket() {
        return socketFactory.call();
    }
}
