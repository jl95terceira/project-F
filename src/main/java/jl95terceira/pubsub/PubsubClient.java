package jl95terceira.pubsub;

import static jl95terceira.lang.stt.uncheck;

import java.net.InetSocketAddress;

import jl95terceira.pubsub.util.NetDefaults;

public class PubsubClient {

    public interface    Options {
        InetSocketAddress brokerAddr();
    }
    public static class EditableOptions implements Options {

        public InetSocketAddress brokerAddr = NetDefaults.brokerAddr;

        @Override public InetSocketAddress brokerAddr() {
            return brokerAddr;
        }
    }

}
