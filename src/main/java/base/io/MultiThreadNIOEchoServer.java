package base.io;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadNIOEchoServer {
    public static Map<Socket, Long> geym_time_stat = new HashMap<Socket, Long>();
    private Selector selector;
    private ExecutorService tp = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        MultiThreadNIOEchoServer echoServer = new MultiThreadNIOEchoServer();
        try {
            echoServer.startServer();
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    @Test
    public void client(){
        Socket client = null;
        PrintWriter writer = null;
        BufferedReader reader = null;
        try {
            client = new Socket();
            client.connect(new InetSocketAddress("localhost", 8000));
            writer = new PrintWriter(client.getOutputStream(), true);
            writer.println("Hello!");
            writer.flush();
            reader = new BufferedReader(new InputStreamReader(
                    client.getInputStream()));
            System.out.println("from server: " + reader.readLine());
        } catch (Exception e) {
        } finally {
            // 省略资源关闭
        }
    }

    private void startServer() throws Exception {
        selector = SelectorProvider.provider().openSelector();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        InetSocketAddress isa = new InetSocketAddress(8000);
        ssc.socket().bind(isa);
        // 注册感兴趣的事件，此处对accpet事件感兴趣
        SelectionKey acceptKey = ssc.register(selector, SelectionKey.OP_ACCEPT);
        for (; ; ) {
            selector.select();
            Set readyKeys = selector.selectedKeys();
            Iterator i = readyKeys.iterator();
            long e = 0;
            while (i.hasNext()) {
                SelectionKey sk = (SelectionKey) i.next();
                i.remove();
                if (sk.isAcceptable()) {
                    doAccept(sk);
                } else if (sk.isValid() && sk.isReadable()) {
                    if (!geym_time_stat.containsKey(((SocketChannel) sk
                            .channel()).socket())) {
                        geym_time_stat.put(
                                ((SocketChannel) sk.channel()).socket(),
                                System.currentTimeMillis());
                    }
                    doRead(sk);
                } else if (sk.isValid() && sk.isWritable()) {
                    doWrite(sk);
                    e = System.currentTimeMillis();
                    long b = geym_time_stat.remove(((SocketChannel) sk
                            .channel()).socket());
                    System.out.println("spend:" + (e - b) + "ms");
                }
            }
        }
    }

    private void doWrite(SelectionKey sk) {
        // TODO Auto-generated method stub
        SocketChannel channel = (SocketChannel) sk.channel();
        EchoClient echoClient = (EchoClient) sk.attachment();
        LinkedList<ByteBuffer> outq = echoClient.getOutputQueue();
        ByteBuffer bb = outq.getLast();
        try {
            int len = channel.write(bb);
            if (len == -1) {
                disconnect(sk);
                return;
            }
            if (bb.remaining() == 0) {
                outq.removeLast();
            }
        } catch (Exception e) {
            // TODO: handle exception
            disconnect(sk);
        }
        if (outq.size() == 0) {
            sk.interestOps(SelectionKey.OP_READ);
        }
    }

    private void doRead(SelectionKey sk) {
        // TODO Auto-generated method stub
        SocketChannel channel = (SocketChannel) sk.channel();
        ByteBuffer bb = ByteBuffer.allocate(8192);
        int len;
        try {
            len = channel.read(bb);
            if (len < 0) {
                disconnect(sk);
                return;
            }
        } catch (Exception e) {
            // TODO: handle exception
            disconnect(sk);
            return;
        }
        bb.flip();
        tp.execute(new HandleMsg(sk, bb));
    }

    private void disconnect(SelectionKey sk) {
        // TODO Auto-generated method stub
        //省略略干关闭操作
    }

    private void doAccept(SelectionKey sk) {
        // TODO Auto-generated method stub
        ServerSocketChannel server = (ServerSocketChannel) sk.channel();
        SocketChannel clientChannel;
        try {
            clientChannel = server.accept();
            clientChannel.configureBlocking(false);
            SelectionKey clientKey = clientChannel.register(selector,
                    SelectionKey.OP_READ);
            EchoClient echoClinet = new EchoClient();
            clientKey.attach(echoClinet);
            InetAddress clientAddress = clientChannel.socket().getInetAddress();
            System.out.println("Accepted connection from "
                    + clientAddress.getHostAddress());
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    class EchoClient {
        private LinkedList<ByteBuffer> outq;

        EchoClient() {
            outq = new LinkedList<ByteBuffer>();
        }

        public LinkedList<ByteBuffer> getOutputQueue() {
            return outq;
        }

        public void enqueue(ByteBuffer bb) {
            outq.addFirst(bb);
        }
    }

    class HandleMsg implements Runnable {
        SelectionKey sk;
        ByteBuffer bb;

        public HandleMsg(SelectionKey sk, ByteBuffer bb) {
            super();
            this.sk = sk;
            this.bb = bb;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            EchoClient echoClient = (EchoClient) sk.attachment();
            echoClient.enqueue(bb);
            sk.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            selector.wakeup();
        }

    }
}
