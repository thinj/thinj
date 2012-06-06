package thinj.linkmodel;

/**
 * This class represents a reference to a class
 * 
 * @author hammer
 * 
 */
public class ClassReference extends ConstantPoolReference {
	private final String aClassName;
	private int aTargetClassId;

	/**
	 * Constructor
	 * 
	 * @param className The fully qualified name of the class
	 * @param classId The id of the referencing class
	 * @param constantPoolIndex The index into the constant pool of the referencing class
	 */
	ClassReference(String className, int classId, int constantPoolIndex) {
		super(classId, constantPoolIndex);
		aClassName = className;
	}

	/**
	 * This method returns the fully qualified name of the referenced class
	 * 
	 * @return
	 */
	public String getClassName() {
		return aClassName;
	}

	@Override
	public String toString() {
		return "ClassReference [aClassName=" + aClassName + "]::" + super.toString();
	}

	/**
	 * This method returns the target class id - is set during linking. Before linking, the return
	 * value is unpredictable
	 * 
	 * @return The target class id
	 */
	public int getTargetClassId() {
		return aTargetClassId;
	}

	/**
	 * This method setsthe target class id. Shall be set during linking.
	 * 
	 * @param targetClassId The target class id
	 */
	public void setTargetClassId(int targetClassId) {
		aTargetClassId = targetClassId;
	}
}
