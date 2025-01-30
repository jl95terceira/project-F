package jl95.pubsub;

import java.net.InetSocketAddress;

import jl95.pubsub.util.Defaults;

public class Client {

    public interface    Options {
        InetSocketAddress brokerAddr();
    }
    public static class EditableOptions implements Options {

        public InetSocketAddress brokerAddr = Defaults.brokerAddr;

        @Override public InetSocketAddress brokerAddr() {
            return brokerAddr;
        }
    }

}
