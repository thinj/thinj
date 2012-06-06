package thinj.linkmodel;

/**
 * This class defines the different array types used in a JVM.
 * 
 * @author hammer
 * 
 */
public class ArrayClassInSuite extends ClassInSuite {
	/**
	 * Constructor
	 * 
	 * @param classType The type as described in the vm spec for instruction 'newarray'
	 */
	public ArrayClassInSuite(ClassTypeEnum classType) {
		super(classType.getSignature(), classType.getArrayType(), classType);
	}
}
