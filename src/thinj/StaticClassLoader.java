package thinj;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import thinj.instructions.AbstractInstruction;
import thinj.linkmodel.ClassInSuite;
import thinj.linkmodel.LinkModel;
import thinj.linkmodel.Member;
import thinj.linkmodel.MemberReference;
import thinj.linkmodel.MethodInClass;
import thinj.linkmodel.MethodOrField;

/**
 * This class contains methods for static class loading, including check of cyclic dependencies in
 * the <clinit> - methods, which will cause the resulting program to fail.
 * 
 * @author hammer
 * 
 */
public class StaticClassLoader {
	// Reference to the link model:
	private final LinkModel aLinkModel;

	// The root node of <clinit> - dependency tree:
	private final TreeNode aRootNode;

	// The list of name of classes in the order they shall be class loaded:
	private LinkedHashSet<String> aClassLoadList;

	/**
	 * Constructor
	 * 
	 * @param linkModel Reference to the link model
	 */
	public StaticClassLoader(LinkModel linkModel) {
		aLinkModel = linkModel;
		aRootNode = new TreeNode("<root>");
		aClassLoadList = new LinkedHashSet<String>();
	}

	/**
	 * This method returns the node corresponding to the class identified by 'className'.
	 * 
	 * @param className Identifies the class
	 * @return A new node is returned if no one existed when calling this method.
	 */
	private TreeNode getNode(String className) {
		TreeNode node = aRootNode.getChildren().get(className);
		if (node == null) {
			node = new TreeNode(className);
			aRootNode.getChildren().put(className, node);
		}

		return node;
	}

	/**
	 * This method first build the dependency tree then generate the class load list by calling the
	 * method {@link #generateClassLoadList(TreeNode)}. All dependencies are kept in the
	 * {@link #aRootNode}, and the Children in a TreeNode just points to elements that are kept in
	 * the {@link #aRootNode}.
	 */
	private void buildClassInitDependencyTree() {
		for (MethodInClass mic : aLinkModel.getAllMethods()) {
			if (mic.getType() == MethodInClass.Type.ClassInitCode && mic.isReferenced()) {
				String childClassName = mic.getMember().getClassName();

				traverseCallTree(childClassName, mic.getMember().getSignature().getName(), mic
						.getMember().getSignature().getDescriptor());
			}
		}

		// First java.lang.Class shall be dumped:
		TreeNode javaLangClassNode = aRootNode.getChildren().get(
				ClassInSuite.getGlobalName(Class.class.getName()));
		if (javaLangClassNode != null) {
			generateClassLoadList(javaLangClassNode);
		}

		// Then the rest:
		generateClassLoadList(aRootNode);
	}

	/**
	 * This method generates the class load list. If a cyclic dependcy is detected this method will
	 * call System.exit
	 * 
	 * @param node The node for which the class load list shall be generated. If a node has already
	 *            been handled, this is a no-op.
	 */
	private void generateClassLoadList(TreeNode node) {
		if (!aClassLoadList.contains(node.getClassName())) {
			if (node.isVisited()) {
				// When a cyclic dependency is detected it is not possible to execute the total
				// class init. Abort linking:
				System.err.println("Cyclic <clinit> dependency: " + node.getClassName());
				System.exit(1);
			} else {
				for (TreeNode child : node.getChildren().values()) {
					node.setVisited(true);
					generateClassLoadList(getNode(child.getClassName()));
					node.setVisited(false);
				}
				if (node != aRootNode) {
					aClassLoadList.add(node.getClassName());
				}
			}
		}
	}

	/**
	 * This method traverses the call graph and calculates the dependencies for the method
	 * identified by the arguments.
	 * 
	 * @param className The class visited
	 * @param methodName The name of the method
	 * @param descriptor The descriptor of the method
	 */
	private void traverseCallTree(String className, String methodName, String descriptor) {
		// Generate class init code sequence:
		ClassInSuite mainClass = aLinkModel.getClassByName(className);
		MethodInClass mainMethod = (MethodInClass) aLinkModel.getMethodOrField(mainClass
				.getClassId(), methodName, descriptor);

		TreeNode node = getNode(className);

		for (MemberReference mref : mainMethod.getMemberReferences()) {
			// Avoid self -references:
			ClassInSuite classReferenced = aLinkModel.getClassByName(mref.getReferencedClassName());
			MethodOrField mof = aLinkModel.getMethodOrField(classReferenced.getClassId(), mref
					.getSignature().getName(), mref.getSignature().getDescriptor());
			if (!mref.getReferencedClassName().equals(className)) {
				node.add(getNode(mref.getReferencedClassName()));
			}
			if (mof instanceof MethodInClass) {
				MethodInClass mic = (MethodInClass) mof;
				if (mic.isStatic()) {
					// depends on clinit + method
					traverseCallTree(mref.getReferencedClassName(), mic.getMember().getSignature()
							.getName(), mic.getMember().getSignature().getDescriptor());
				} else {
					// depends on clinit only
				}
			} else {
				// depends on clinit only

			}
		}
	}

	/**
	 * This method creates the code that calls all <clinit> - methods and finally the main method
	 * 
	 * @param mainClassName The name of the class containing the main method
	 * @return The method containing the code
	 */
	public MethodInClass createInitCode(String mainClassName) {
		// Determine the sequence of <clinit> - methods:
		buildClassInitDependencyTree();

		LinkedList<Integer> initCode = new LinkedList<Integer>();

		ClassInSuite mainClass = aLinkModel.getClassByName(mainClassName);

		for (String className : aClassLoadList) {
			// Generate class init code sequence:
			for (MethodInClass mic : aLinkModel.getAllMethods()) {
				if (mic.getType() == MethodInClass.Type.ClassInitCode
						&& mic.getMember().getClassName().equals(className)) {
					// Invoke static all <clinit> - methods:
					appendInvokeStatic(mic.getMember(), initCode, mainClass.getClassId());
				}
			}
		}

		Member mainMember = new Member(mainClassName, "main", "()V");

		// Invoke static the main() - method:
		appendInvokeStatic(mainMember, initCode, mainClass.getClassId());

		// Make the JVM halt when main returns:
		initCode.addLast(AbstractInstruction.getOpcode(AbstractInstruction.I_vreturn.class));

		byte[] initCodeBytes = new byte[initCode.size()];
		for (int i = 0; i < initCode.size(); i++) {
			initCodeBytes[i] = (byte) (initCode.get(i) & 0xff);
		}

		// Generate the init - method:
		MethodInClass initMethod = aLinkModel.createMethodInClass(mainClassName, "<jvminit>",
				"()V", initCodeBytes, 0, 0, true);

		// Ensure that the init-method is really included in the generated suite:
		initMethod.referenced();

		return initMethod;
	}

	/**
	 * This method adds a reference to a method identified by 'member' to the link model, and an
	 * 'invokestatic' byte code sequence to 'initCode'
	 * 
	 * @param member The referenced member
	 * @param referencingClassId The referencing class id
	 * @param initCode
	 */
	private void appendInvokeStatic(Member member, LinkedList<Integer> initCode,
			int referencingClassId) {
		int constantPoolIndex = getNextAvailableConstantPoolIndex(referencingClassId);
		// Create reference to main - method in model:
		MemberReference ref = aLinkModel.createMemberReference(member.getClassName(), member
				.getSignature(), referencingClassId, constantPoolIndex);
		ref.referenced();
		initCode.addLast(0xb8); // Invokestatic
		initCode.addLast((constantPoolIndex >> 8) & 0xff); // MSByte
		initCode.addLast(constantPoolIndex & 0xff); // LSByte
	}

	/**
	 * This method finds a free constant pool index for class index 'referencingClassId'
	 * 
	 * @param referencingClassId The class wherein a free constant pool index shall be generated
	 * 
	 * @return a free constant pool index
	 */
	private int getNextAvailableConstantPoolIndex(int referencingClassId) {
		int firstFreeConstantPoolIndex = 0;
		for (MemberReference ref : aLinkModel.getAllMethodOrFieldReferences()) {
			if (ref.getClassId() == referencingClassId) {
				if (ref.getConstantPoolIndex() > firstFreeConstantPoolIndex) {
					firstFreeConstantPoolIndex = ref.getConstantPoolIndex();
				}
			}
		}
		firstFreeConstantPoolIndex++;
		return firstFreeConstantPoolIndex;
	}

	/**
	 * This class holds the relation between class name and the classes the <clinit> - method in
	 * class depends on. Note that the constructor shall be used for constructing the root node and
	 * in the {@link StaticClassLoader#getNode(String)} method only.
	 * 
	 * @author hammer
	 * 
	 */
	private static class TreeNode {
		private String aClassName;
		private TreeMap<String, TreeNode> aChildren;
		private boolean aVisited;

		/**
		 * Constructor
		 * 
		 * @param className The name of the class. The root node should use a non-null dummy name.
		 */
		public TreeNode(String className) {
			aClassName = className;
			aChildren = new TreeMap<String, TreeNode>(new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
			setVisited(false);
		}

		/**
		 * This method adds a dependency
		 * 
		 * @param child Identifies the class that this class depends on.
		 */
		public void add(TreeNode child) {
			aChildren.put(child.getClassName(), child);

		}

		/**
		 * This method returns the name of class that this instance represents
		 * 
		 * @return The name of class that this instance represents
		 */
		public String getClassName() {
			return aClassName;
		}

		/**
		 * This method returns all the dependencies of this class. Note that there is copy involved;
		 * the returned collection will be a subset of elements in the root node
		 * 
		 * @return All the dependencies of this class
		 */
		public TreeMap<String, TreeNode> getChildren() {
			return aChildren;
		}

		/**
		 * This method shall be used when detecting cyclic dependency: Set it before descending into
		 * this node, and clear it when leaving the node again.
		 * 
		 * @param visited true, if the node has been visited during
		 */
		public void setVisited(boolean visited) {
			aVisited = visited;
		}

		/**
		 * This method returns the visited flag
		 * 
		 * @return The visited flag
		 */
		public boolean isVisited() {
			return aVisited;
		}
	}
}
