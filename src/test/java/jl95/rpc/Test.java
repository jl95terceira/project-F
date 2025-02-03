package jl95.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class Test {

    @org.junit.Test
    public void test() throws Exception {
        var requesterFuture = CompletableFuture.supplyAsync(() -> StringRequester.get(Requester.Options.defaultsAsServer()), (task) -> new Thread(task).start());
        Thread.sleep(50);
        var responderFuture = CompletableFuture.supplyAsync(() -> StringResponder.get(Responder.Options.defaultsAsClient()), (task) -> new Thread(task).start());
        var requester = requesterFuture.get();
        var responder = responderFuture.get();
        responder.start(msg -> msg + " world");
        org.junit.Assert.assertEquals("hello world", requester.call("hello"));
        responder.stop();
    }
}
