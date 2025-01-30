package jl95.pubsub.util;

public enum MessageType {

    PUBLISH                 (""),
    REQ_CLOSE               ("C"),
    REQ_SUBSCRIPTION_BY_LIST("S"),
    REQ_SUBSCRIPTION_TO_ALL ("A"),
    REQ_SUBSCRIPTION_TO_NONE("N");

    public final String serial;
    MessageType(String serial) {this.serial = serial;}
}
