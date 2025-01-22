package jl95terceira.pubsub.util;

public enum RequestType {

    CLOSE               ("close"),
    SUBSCRIPTION_BY_LIST("subsList"),
    SUBSCRIPTION_TO_ALL ("subsAll"),
    SUBSCRIPTION_TO_NONE("subsNone");

    public final String serial;
    RequestType(String serial) {this.serial = serial;}
}
