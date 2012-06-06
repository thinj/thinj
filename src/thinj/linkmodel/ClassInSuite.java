package thinj.linkmodel;

import java.util.LinkedList;

/**
 * This class contains the mapping between a class id and the corresponding class name.
 * 
 */
public class ClassInSuite {
	private final String aClassName;
	private int aClassId;
	private final String aSuperClassName;
	private int aInstanceSize;
	private boolean aReferenced;
	private final LinkedList<String> aInterfaces;
	private final ClassTypeEnum aClassType;

	/**
	 * Constructor for array class
	 * 
	 * @param className The name of the class
	 * @param classId The integer key for the class
	 * @param classType The type of the class
	 * @param elementClassId The classId of the elements. Ignored if the array is an array of simple
	 *            types
	 */
	public ClassInSuite(String className, int classId, ClassTypeEnum classType) {
		this(className, classId, Object.class.getName(), classType);
	}

	/**
	 * Constructor
	 * 
	 * @param className The name of the class
	 * @param classId The integer key for the class
	 * @param superClassName The name of the super class
	 * @param classType The type of the class
	 */
	public ClassInSuite(String className, int classId, String superClassName,
			ClassTypeEnum classType) {
		aClassType = classType;
		aSuperClassName = superClassName != null ? getGlobalName(superClassName) : null;
		aClassName = getGlobalName(className);
		aClassId = classId;
		aReferenced = false;
		aInterfaces = new LinkedList<String>();
	}

	/**
	 * Returns the name of the super class.
	 * 
	 * @return The name of the super class. If this class is 'java.lang.Object', null is returned.
	 */
	public String getSuperClassName() {
		return aSuperClassName;
	}

	/**
	 * This method returns the names of all implemented interfaces
	 * 
	 * @return The names of all implemented interfaces
	 */
	public String[] getImplementedInterfaces() {
		return aInterfaces.toArray(new String[aInterfaces.size()]);
	}

	/**
	 * This method adds the name of an implemented interface
	 * 
	 * @param className The name of an implemented interface
	 */
	public void addImplementedInterface(String className) {
		aInterfaces.add(className);
	}

	public String getClassName() {
		return aClassName;
	}

	public int getClassId() {
		return aClassId;
	}

	/**
	 * This method sets the class id
	 * 
	 * @param classId The new classId
	 */
	public void renumberClassId(int classId) {
		aClassId = classId;
	}

	/**
	 * This method converts a dot'ed class name to a slash'ed ditto. The slash'ed version is the
	 * version used in all linking etc. aspects.
	 * 
	 * @param className The using dot fully qualified class name
	 * @return The slash'ed version of the class name
	 */
	public static String getGlobalName(String className) {
		return className.replace(".", "/");
	}

	/**
	 * This method sets the size of an instance of this class (excluding super classes sizes)
	 * 
	 * @param instanceSize The size of an instance of this class (excluding super classes sizes)
	 */
	public void setInstanceSize(int instanceSize) {
		aInstanceSize = instanceSize;
	}

	/**
	 * This method returns the size of an instance of this class (excluding super classes sizes)
	 * 
	 * @return The size of an instance of this class (excluding super classes sizes)
	 */
	public int getInstanceSize() {
		return aInstanceSize;
	}

	/**
	 * This method returns the size of an instance of this class (including super classes sizes)
	 * 
	 * @param linkModel Used for getting size of super classes
	 * @return The size of an instance of this class (including super classes sizes)
	 */
	public int getInstanceSizeWithSuper(LinkModel linkModel) {
		int instanceSize = getInstanceSize();
		if (aSuperClassName != null) {
			instanceSize += linkModel.getClassByName(aSuperClassName).getInstanceSizeWithSuper(
					linkModel);
		}

		return instanceSize;
	}

	/**
	 * This mark this class as referenced. Some part of it is needed in the resulting suite.
	 */
	public void referenced() {
		aReferenced = true;
	}

	/**
	 * This method returns true, if this class should be linked into the resulting suite.
	 * 
	 * @return true, if this class should be linked into the resulting suite; false otherwise.
	 */
	public boolean isReferenced() {
		return aReferenced;
	}

	/**
	 * This method returns true if and only this class is a direct descendant from 'cis'
	 * 
	 * @param cis
	 * @return true if and only this class is a direct descendant from 'cis'
	 */
	public boolean isExtending(ClassInSuite cis) {
		boolean isExt;
		if (aSuperClassName == null) {
			// This class is java.lang.Object - this does not inherit from any other
			// classes:
			isExt = false;
		} else {
			isExt = aSuperClassName.equals(cis.getClassName());
		}

		return isExt;
	}

	/**
	 * This method returns true if and only if this class is implementing the supplied
	 * interfaceClass
	 * 
	 * @param interfaceClass The interface to test for
	 * @return true if and only if this class is implementing the supplied interfaceClass
	 */
	public boolean isImplementing(ClassInSuite interfaceClass) {
		return aInterfaces.contains(interfaceClass.getClassName());
	}

	/**
	 * This method returns the class type of this class
	 * 
	 * @return The class type of this class
	 */
	public ClassTypeEnum getClassType() {
		return aClassType;
	}
}
