package base.io;


import org.junit.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class IO {

    @Test
    public void nio() {
        RandomAccessFile aFile = null;
        // 文件编码是utf8,需要用utf8解码
        Charset charset = Charset.forName("utf-8");
        CharsetDecoder decoder = charset.newDecoder();
        try {
            aFile = new RandomAccessFile(getClass().getClassLoader().getResource(".").getPath() + File.separator + "test_file/nio.txt", "rw");
            FileChannel fileChannel = aFile.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            CharBuffer charBuffer = CharBuffer.allocate(4);
            int bytesRead;
            int leftNum = 0; // 未转码的字节数
            char[] tmp = null; // 临时存放转码后的字符
            byte[] remainByte = null;// 存放decode操作后未处理完的字节。decode仅仅转码尽可能多的字节，此次转码不了的字节需要缓存，下次再转
//            while ((bytesRead = fileChannel.read(byteBuffer)) != -1) {
//                byteBuffer.flip();
//                decoder.decode(byteBuffer, charBuffer, true);
//                charBuffer.flip(); // 切换buffer从写模式到读模式
//                remainByte = null;
//                leftNum = byteBuffer.limit() - byteBuffer.position();
//                if (leftNum > 0) { // 记录未转换完的字节
//                    remainByte = new byte[leftNum];
//                    byteBuffer.get(remainByte, 0, leftNum);
//                }
//
//                // 输出已转换的字符
//                tmp = new char[charBuffer.length()];
//                while (charBuffer.hasRemaining()) {
//                    charBuffer.get(tmp);
//                    System.out.print(new String(tmp));
//                }
//                byteBuffer.clear(); // 切换buffer从读模式到写模式
//                charBuffer.clear(); // 切换buffer从读模式到写模式
//                if (remainByte != null) {
//                    byteBuffer.put(remainByte); // 将未转换完的字节写入bBuf，与下次读取的byte一起转换
//                }
//            }
            int count = fileChannel.read(byteBuffer);
            while (count != -1) {
                remainByte = null;
//                System.out.println("count = "+count);
                byteBuffer.flip();
                decoder.decode(byteBuffer, charBuffer, false);
                charBuffer.flip();
                if ((leftNum = byteBuffer.limit() - byteBuffer.position()) > 0) {
                    remainByte = new byte[leftNum];
                    byteBuffer.get(remainByte, 0, leftNum);
                }
                while (charBuffer.hasRemaining()) {
                    System.out.print(charBuffer.get());
                }
//                System.out.println();

                byteBuffer.clear();
                charBuffer.clear();
                if (remainByte != null) {
                    byteBuffer.put(remainByte);
                }
                count = fileChannel.read(byteBuffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (aFile != null) {
                    aFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void io() {
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            in = new BufferedReader(new FileReader(getClass().getClassLoader().getResource(".").getPath() + File.separator + "test_file/io.txt"));
            out = new BufferedWriter(new FileWriter(getClass().getClassLoader().getResource(".").getPath() + File.separator + "test_file/new_io.txt"));
            char[] buf = new char[15];
            int hasRead = 0;
            while ((hasRead = in.read(buf)) != -1) {
                System.out.print(new String(buf, 0, hasRead));
                out.write(buf, 0, hasRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void writeObject() {
        OutputStream outputStream = null;
        BufferedOutputStream buf = null;
        ObjectOutputStream obj = null;
        try {
            //序列化文件輸出流
            outputStream = new FileOutputStream(getClass().getClassLoader().getResource(".").getPath() + File.separator + "test_file/writeObject.tmp");
            //构建缓冲流
            buf = new BufferedOutputStream(outputStream);
            //构建字符输出的对象流
            obj = new ObjectOutputStream(buf);
            //序列化数据写入
            obj.writeObject(new Person("A", 21));//Person对象
            //关闭流
            obj.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void readObject() {
        try {
            URL resource = this.getClass().getClassLoader().getResource("test_file/writeObject.tmp");
            if (resource == null) return;
            InputStream inputStream = new FileInputStream(resource.getFile());
            //构建缓冲流
            BufferedInputStream buf = new BufferedInputStream(inputStream);
            //构建字符输入的对象流
            ObjectInputStream obj = new ObjectInputStream(buf);
            Person tempPerson = (Person) obj.readObject();
            System.out.println("Person对象为：" + tempPerson);
            //关闭流
            obj.close();
            buf.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void workFileTree() {

        Path startingDir = Paths.get("src");
        List<Path> result = new LinkedList<Path>();
        try {
            Files.walkFileTree(startingDir, new FindJavaVisitor(result));
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.stream().forEach(path -> {
            System.out.println(path.toAbsolutePath().normalize());
        });
    }

    @Test
    public void fileChannelTest() {
        //1.创建一个RandomAccessFile（随机访问文件）对象，
        RandomAccessFile raf = null;
        try {
            System.out.println();
            raf = new RandomAccessFile(this.getClass().getClassLoader().getResource("test_file/niodata.txt").getFile(), "rw");
            //通过RandomAccessFile对象的getChannel()方法。FileChannel是抽象类。
            FileChannel inChannel = raf.getChannel();
            System.out.println(inChannel.size());
            inChannel.position(inChannel.size());
            //2.创建一个读数据缓冲区对象
            ByteBuffer buf = ByteBuffer.allocate(2);
            //3.从通道中读取数据
            //创建一个写数据缓冲区对象
            ByteBuffer buf2 = ByteBuffer.allocate(48);
            //写入数据
            buf2.put("filechannel test".getBytes());
            buf2.flip();
            inChannel.write(buf2);
            inChannel.position(0);
            int bytesRead = inChannel.read(buf);

//            byte[] text = new byte[20];
            while ((bytesRead) != -1) {
//                System.out.println("Read " + bytesRead);
//                System.out.println("remaining " + buf.remaining());
//                //Buffer有两种模式，写模式和读模式。在写模式下调用flip()之后，Buffer从写模式变成读模式。
                buf.flip();
//                //如果还有未读内容
                while (buf.hasRemaining()) {
                    System.out.print((char) buf.get());
//                System.out.print(buf.get(text, 0, buf.remaining()));
                }
//                清空缓存区
                buf.clear();
                bytesRead = inChannel.read(buf);
            }
            //关闭RandomAccessFile（随机访问文件）对象
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void nioBuffer() {
        //分配缓冲区（Allocating a Buffer）

        ByteBuffer buffer = ByteBuffer.allocate(33);

        System.out.println("-------------Test reset-------------");

        //clear()方法，position将被设回0，limit被设置成 capacity的值

        buffer.clear();

        // 设置这个缓冲区的位置

        buffer.position(5);

        //将此缓冲区的标记设置在其位置。没有buffer.mark();这句话会报错

        buffer.mark();

        buffer.position(10);

        System.out.println("before reset:      " + buffer);

        //将此缓冲区的位置重置为先前标记的位置。（buffer.position(5)）

        buffer.reset();

        System.out.println("after reset:       " + buffer);

        System.out.println("-------------Test rewind-------------");

        buffer.clear();

        buffer.position(10);

        //返回此缓冲区的限制。

        buffer.limit(15);

        System.out.println("before rewind:       " + buffer);

        //把position设为0，mark设为-1，不改变limit的值

        buffer.rewind();

        System.out.println("before rewind:       " + buffer);

        System.out.println("-------------Test compact-------------");

        buffer.clear();

        buffer.put("abcd".getBytes());

        System.out.println("before compact:       " + buffer);

        System.out.println(new String(buffer.array()));

        //limit = position;position = 0;mark = -1; 翻转，也就是让flip之后的position到limit这块区域变成之前的0到position这块，

        //翻转就是将一个处于存数据状态的缓冲区变为一个处于准备取数据的状态

        buffer.flip();

        System.out.println("after flip:       " + buffer);

        //get()方法：相对读，从position位置读取一个byte，并将position+1，为下次读写作准备

        System.out.println((char) buffer.get());

        System.out.println((char) buffer.get());

        System.out.println((char) buffer.get());

        System.out.println("after three gets:       " + buffer);

        System.out.println("\t" + new String(buffer.array()));

        //把从position到limit中的内容移到0到limit-position的区域内，position和limit的取值也分别变成limit-position、capacity。

        // 如果先将positon设置到limit，再compact，那么相当于clear()

        buffer.compact();

        System.out.println("after compact:       " + buffer);

        System.out.println("\t" + new String(buffer.array()));

        System.out.println("-------------Test get-------------");

        buffer = ByteBuffer.allocate(32);

        buffer.put((byte) 'a').put((byte) 'b').put((byte) 'c').put((byte) 'd')

                .put((byte) 'e').put((byte) 'f');

        System.out.println("before flip():       " + buffer);

        // 转换为读取模式

        buffer.flip();

        System.out.println("before get():       " + buffer);

        System.out.println((char) buffer.get());

        System.out.println("after get():       " + buffer);

        // get(index)不影响position的值

        System.out.println((char) buffer.get(2));

        System.out.println("after get(index):       " + buffer);

        byte[] dst = new byte[10];

        buffer.get(dst, 0, 2);

        System.out.println("after get(dst, 0, 2):       " + buffer);

        System.out.println("\t dst:" + new String(dst));

        System.out.println("buffer now is:       " + buffer);

        System.out.println("\t" + new String(buffer.array()));

        System.out.println("-------------Test put-------------");

        ByteBuffer bb = ByteBuffer.allocate(32);

        System.out.println("before put(byte):       " + bb);

        System.out.println("after put(byte):       " + bb.put((byte) 'z'));

        System.out.println("\t" + bb.put(2, (byte) 'c'));

        // put(2,(byte) 'c')不改变position的位置

        System.out.println("after put(2,(byte) 'c'):       " + bb);

        System.out.println("\t" + new String(bb.array()));

        // 这里的buffer是 abcdef[pos=3 lim=6 cap=32]

        bb.put(buffer);

        System.out.println("after put(buffer):       " + bb);

        System.out.println("\t" + new String(bb.array()));
    }

    /**
     * 服务端
     */
    @Test
    public void serverSocketChannel() {
        try {
            //1.通过ServerSocketChannel 的open()方法创建一个ServerSocketChannel对象，open方法的作用：打开套接字通道
            ServerSocketChannel ssc = ServerSocketChannel.open();
            //2.通过ServerSocketChannel绑定ip地址和port(端口号)
            ssc.socket().bind(new InetSocketAddress("127.0.0.1", 3333));
            //通过ServerSocketChannelImpl的accept()方法创建一个SocketChannel对象用户从客户端读/写数据
            SocketChannel socketChannel = ssc.accept();
            //3.创建写数据的缓存区对象
            ByteBuffer writeBuffer = ByteBuffer.allocate(128);
            writeBuffer.put("hello WebClient this is from WebServer".getBytes());
            writeBuffer.flip();
            socketChannel.write(writeBuffer);
            //创建读数据的缓存区对象
            ByteBuffer readBuffer = ByteBuffer.allocate(128);
            //读取缓存区数据
            socketChannel.read(readBuffer);
            StringBuilder stringBuffer = new StringBuilder();
            //4.将Buffer从写模式变为可读模式
            readBuffer.flip();
            while (readBuffer.hasRemaining()) {
                stringBuffer.append((char) readBuffer.get());
            }
            System.out.println("从客户端接收到的数据：" + stringBuffer);
            socketChannel.close();
            ssc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 客户端
     */
    @Test
    public void socketChannel() {
        try {
            //1.通过SocketChannel的open()方法创建一个SocketChannel对象
            SocketChannel socketChannel = SocketChannel.open();
            //2.连接到远程服务器（连接此通道的socket）
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 3333));
            // 3.创建写数据缓存区对象
            ByteBuffer writeBuffer = ByteBuffer.allocate(128);
            writeBuffer.put("hello WebServer this is from WebClient".getBytes());
            writeBuffer.flip();
            socketChannel.write(writeBuffer);
            //创建读数据缓存区对象
            ByteBuffer readBuffer = ByteBuffer.allocate(128);
            socketChannel.read(readBuffer);
            //String 字符串常量，不可变；StringBuffer 字符串变量（线程安全），可变；StringBuilder 字符串变量（非线程安全），可变
            StringBuilder stringBuffer = new StringBuilder();
            //4.将Buffer从写模式变为可读模式
            readBuffer.flip();
            while (readBuffer.hasRemaining()) {
                stringBuffer.append((char) readBuffer.get());
            }
            System.out.println("从服务端接收到的数据：" + stringBuffer);
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void datagramChannel() {

    }

    @Test
    public void datagramChannelClient() {

    }

    @Test
    public void serverSelector() {
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8000));
            ssc.configureBlocking(false);
            Selector selector = Selector.open();
            //注册channel，并且指定感兴趣的事件是Accept
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            ByteBuffer readBuff = ByteBuffer.allocate(1024);
            ByteBuffer writeBuff = ByteBuffer.allocate(128);
            writeBuff.put("received".getBytes());
            writeBuff.flip();
            while (true) {
                int nReady = selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (key.isAcceptable()) {
                        //创建新的连接，并且把连接注册到selector上，而且，//声明这个channel只对读操作感兴趣。
                        SocketChannel socketChannel = ssc.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector,
                                SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        readBuff.clear();
                        socketChannel.read(readBuff);
                        readBuff.flip();
                        System.out.println("received : " + new String(readBuff.array()));
                        key.interestOps(SelectionKey.OP_WRITE);
                    } else if (key.isWritable()) {
                        writeBuff.rewind();
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        socketChannel.write(writeBuff);
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void clientSelector() {
        try{
            SocketChannel socketChannel =SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("127.0.0.1",8000));
            ByteBuffer writeBuffer =ByteBuffer.allocate(32);
            ByteBuffer readBuffer =ByteBuffer.allocate(32);
            writeBuffer.put("hello".getBytes());
            writeBuffer.flip();
            while(true){
                writeBuffer.rewind();
                socketChannel.write(writeBuffer);
                readBuffer.clear();
                socketChannel.read(readBuffer);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 内存映射和普通IO对比，差10倍左右
     */
    @Test
    public void memMapAndIO1(){
        try {
            RandomAccessFile file = new RandomAccessFile("D:\\文件\\书籍\\Java\\JAVA核心\\深入理解Java虚拟机 JVM高级特性与最佳实践.pdf","rw");
            FileChannel channel = file.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY,0,channel.size());
            ByteBuffer buffer1 = ByteBuffer.allocate(1024);
            byte[] b = new byte[1024];

            long len = file.length();
            long startTime = System.currentTimeMillis();
            //读取内存映射文件
            for(int i=0;i<file.length();i+=1024*10){
                if (len - i > 1024) {
                    buffer.get(b);
                } else {
                    buffer.get(new byte[(int)(len - i)]);
                }
            }
            long endTime = System.currentTimeMillis();
            System.out.println("使用内存映射方式读取文件总耗时： "+(endTime - startTime));


            //普通IO流方式
            long startTime1 = System.currentTimeMillis();
            while(channel.read(buffer1) > 0){
                buffer1.flip();
                buffer1.clear();
            }

            long endTime1 = System.currentTimeMillis();
            System.out.println("使用普通IO流方式读取文件总耗时： "+(endTime1 - startTime1));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
    -------------------------------------------------------------
     */


    static class Person implements Serializable {


        private static final long serialVersionUID = 1882774242728205866L;
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

    private class FindJavaVisitor extends SimpleFileVisitor<Path> {

        private List<Path> result;

        public FindJavaVisitor(List<Path> result) {
            this.result = result;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
//            if (file.toString().endsWith(".java")) {
            result.add(file.getFileName());
//            }
            return FileVisitResult.CONTINUE;
        }


    }

}
