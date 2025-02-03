package jl95.rpc.util;

import jl95.rpc.Requester;

public abstract class RequesterEditableOptionsPartial implements Requester.Options {

    public Integer responseTimeoutMs = Defaults.responseTimeoutMs;

    @Override public Integer getResponseTimeoutMs() { return responseTimeoutMs; }
}
