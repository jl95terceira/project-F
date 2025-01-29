package jl95terceira.net;

import static java.lang.String.*;

import java.util.*;

public class Test {

    private static java.net.InetSocketAddress addr = new java.net.InetSocketAddress("127.0.0.1", 42422);
    private static Boolean toStop = false;

    static { Runtime.getRuntime().addShutdownHook(new Thread(() -> { toStop = true; })); }

    // toy main
    public static void main(String[] args) throws Exception {

        System.out.println(format("Address: %s", addr));
        var serversock = new java.net.ServerSocket();
        serversock.bind(addr);
        new Thread(() -> {
            try {
                var sock = serversock.accept();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        sock.close();
                    }
                    catch(Exception ex) {}
                }));
                StringReceiverBySocket.get(sock).recv(System.out::println);
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).start();
        var client = new java.net.Socket();
        client.connect(addr);
        while (!toStop) {
             StringSenderBySocket.get(client).send(format("Hello, at %s;", java.time.Instant.now()));
             Thread.sleep(1000);
        }
        System.out.println("done");
    }

    @org.junit.Test public void test() throws Exception {

        List<String> messagesSend = new ArrayList<>(100);
        for (int i = 0; i < 1000; i++) {
            messagesSend.add(UUID.randomUUID().toString().repeat(1000));
        }
        System.out.printf("Testing send-receive (through localhost) for %s messages%n", messagesSend.size());
        var serversock = new java.net.ServerSocket();
        serversock.bind(addr);
        int[] charsReceivedNr = { 0 };
        new Thread(() -> {
            try {
                var sock = serversock.accept();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        sock.close();
                    }
                    catch(Exception ex) {}
                }));
                var messagesSendIterator = messagesSend.iterator();
                StringReceiverBySocket.get(sock).recv(message -> {
                    charsReceivedNr[0] += message.length();
                    org.junit.Assert.assertTrue  (messagesSendIterator.hasNext());
                    org.junit.Assert.assertEquals(messagesSendIterator.next   (), message);
                });
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).start();
        var client = new java.net.Socket();
        client.connect(addr);
        for (var message: messagesSend) {
             StringSenderBySocket.get(client).send(message);
        }
        System.out.println("Exchanged a total of "+charsReceivedNr[0]+" characters");
        System.out.println("OK!");
    }
}
