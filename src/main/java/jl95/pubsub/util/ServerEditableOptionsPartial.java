package jl95.pubsub.util;

import jl95.lang.variadic.Method0;
import jl95.lang.variadic.Method1;
import jl95.pubsub.Server;

public abstract class ServerEditableOptionsPartial implements Server.Options {

    public Method1<Exception> acceptErrorCb   = (ex) -> System.out.printf("Error on accept connection: %s%n", ex);
    public Method0            acceptTimeoutCb = ()   -> {
    };

    @Override public void onAcceptError  (Exception ex) {
        acceptErrorCb.call(ex);
    }
    @Override public void onAcceptTimeout()             { acceptTimeoutCb.call(); }
}
