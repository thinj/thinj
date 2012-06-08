package thinj.instructions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import thinj.IntInABox;
import thinj.BuildinDependency;
import thinj.linkmodel.ClassReference;
import thinj.linkmodel.ClassTypeEnum;
import thinj.linkmodel.ConstantReference;
import thinj.linkmodel.LinkModel;
import thinj.linkmodel.MemberReference;
import thinj.linkmodel.MethodInClass;
import thinj.linkmodel.Signature;

public abstract class AbstractInstruction {
	public static final String BYTECODE_PREFIX = "c_";
	private static final String C_FUNCTION_PREFIX = "f_";

	// registered instructions:
	protected static HashMap<Integer, Class<? extends AbstractInstruction>> aInstructions = new HashMap<Integer, Class<? extends AbstractInstruction>>();
	private final int aSize;
	protected byte[] aCode;

	// Referenced instructions - key is the VM SPEC'ed opcode; value is the translate
	// opcode. This is used for renumbering of opcodes and elimination of
	// unused instructions:
	private static HashMap<Integer, Integer> aInstructionCodeMapping;
	private static TreeMap<Integer, Integer> aReverseInstructionMapping;

	// Generator for instruction codes - this is used for renumbering of opcodes and elimination of
	// unused instructions:
	private static int aInstructionCodeGenerator;

	static {
		aInstructionCodeMapping = new HashMap<Integer, Integer>();
		aReverseInstructionMapping = new TreeMap<Integer, Integer>();
		aInstructionCodeGenerator = 0;

		aInstructions.put(0x00, I_nop.class);
		aInstructions.put(0x01, I_aconst_null.class);
		aInstructions.put(0x02, I_iconst_m1.class);
		aInstructions.put(0x03, I_iconst_0.class);
		aInstructions.put(0x04, I_iconst_1.class);
		aInstructions.put(0x05, I_iconst_2.class);
		aInstructions.put(0x06, I_iconst_3.class);
		aInstructions.put(0x07, I_iconst_4.class);
		aInstructions.put(0x08, I_iconst_5.class);

		aInstructions.put(0x10, I_bipush.class);
		aInstructions.put(0x11, I_sipush.class);
		aInstructions.put(0x12, I_ldc.class);

		aInstructions.put(0x15, I_iload.class);

		aInstructions.put(0x19, I_aload.class);
		aInstructions.put(0x1a, I_iload_0.class);
		aInstructions.put(0x1b, I_iload_1.class);
		aInstructions.put(0x1c, I_iload_2.class);
		aInstructions.put(0x1d, I_iload_3.class);

		aInstructions.put(0x2a, I_aload_0.class);
		aInstructions.put(0x2b, I_aload_1.class);
		aInstructions.put(0x2c, I_aload_2.class);
		aInstructions.put(0x2d, I_aload_3.class);
		aInstructions.put(0x2e, I_iaload.class);

		aInstructions.put(0x32, I_aaload.class);
		aInstructions.put(0x33, I_baload.class);
		aInstructions.put(0x34, I_caload.class);

		aInstructions.put(0x36, I_istore.class);

		aInstructions.put(0x3a, I_astore.class);
		aInstructions.put(0x3b, I_istore_0.class);
		aInstructions.put(0x3c, I_istore_1.class);
		aInstructions.put(0x3d, I_istore_2.class);
		aInstructions.put(0x3e, I_istore_3.class);

		aInstructions.put(0x4b, I_astore_0.class);
		aInstructions.put(0x4c, I_astore_1.class);
		aInstructions.put(0x4d, I_astore_2.class);
		aInstructions.put(0x4e, I_astore_3.class);
		aInstructions.put(0x4f, I_iastore.class);

		aInstructions.put(0x53, I_aastore.class);
		aInstructions.put(0x54, I_bastore.class);
		aInstructions.put(0x55, I_castore.class);
		aInstructions.put(0x57, I_pop.class);

		aInstructions.put(0x59, I_dup.class);

		aInstructions.put(0x5c, I_dup2.class);

		aInstructions.put(0x60, I_iadd.class);

		aInstructions.put(0x64, I_isub.class);

		aInstructions.put(0x68, I_imul.class);

		aInstructions.put(0x6c, I_idiv.class);

		aInstructions.put(0x70, I_irem.class);

		aInstructions.put(0x74, I_ineg.class);

		aInstructions.put(0x78, I_ishl.class);

		aInstructions.put(0x7a, I_ishr.class);

		aInstructions.put(0x7e, I_iand.class);

		aInstructions.put(0x80, I_ior.class);

		aInstructions.put(0x82, I_ixor.class);

		aInstructions.put(0x84, I_iinc.class);

		aInstructions.put(0x91, I_i2b.class);

		aInstructions.put(0x99, I_ifeq.class);
		aInstructions.put(0x9a, I_ifne.class);

		aInstructions.put(0x9b, I_iflt.class);
		aInstructions.put(0x9c, I_ifge.class);
		aInstructions.put(0x9d, I_ifgt.class);
		aInstructions.put(0x9e, I_ifle.class);
		aInstructions.put(0x9f, I_if_icmpeq.class);
		aInstructions.put(0xa0, I_if_icmpne.class);
		aInstructions.put(0xa1, I_if_icmplt.class);
		aInstructions.put(0xa2, I_if_icmpge.class);
		aInstructions.put(0xa3, I_if_icmpgt.class);
		aInstructions.put(0xa4, I_if_icmple.class);
		aInstructions.put(0xa5, I_if_acmpeq.class);
		aInstructions.put(0xa6, I_if_acmpne.class);
		aInstructions.put(0xa7, I_goto.class);

		aInstructions.put(0xac, I_ireturn.class);

		aInstructions.put(0xb0, I_areturn.class);
		aInstructions.put(0xb1, I_vreturn.class);
		aInstructions.put(0xb2, I_getstatic.class);
		aInstructions.put(0xb3, I_putstatic.class);
		aInstructions.put(0xb4, I_getfield.class);
		aInstructions.put(0xb5, I_putfield.class);
		aInstructions.put(0xb6, I_invokevirtual.class);
		aInstructions.put(0xb7, I_invokespecial.class);
		aInstructions.put(0xb8, I_invokestatic.class);
		aInstructions.put(0xb9, I_invokeinterface.class);
		aInstructions.put(0xba, I_unused.class);
		aInstructions.put(0xbb, I_new.class);
		aInstructions.put(0xbc, I_newarray.class);
		aInstructions.put(0xbd, I_anewarray.class);
		aInstructions.put(0xbe, I_arraylength.class);
		aInstructions.put(0xbf, I_athrow.class);
		aInstructions.put(0xc0, I_checkcast.class);
		aInstructions.put(0xc1, I_instanceof.class);
		// aInstructions.put(0xc2, I_monitorenter.class);
		// aInstructions.put(0xc3, I_monitorexit.class);

		aInstructions.put(0xc6, I_ifnull.class);
		aInstructions.put(0xc7, I_ifnonnull.class);

	}

	public AbstractInstruction(int size) {
		aSize = size;
	}

	/**
	 * This method shall register the dependencies that this instruction have. A typical dependency
	 * might be a method, class, field or exception etc.<br/>
	 * Note! Registering a dependency does not necessarily mean that the dependency is indeed
	 * included in the final suite; it is only included if the method identified by classId/mic is
	 * included in the suite.
	 * 
	 * 
	 * @param linkModel The link model
	 * @param referencingClassId The class containing the method 'mic'
	 * @param mic The method containing this instruction
	 * @param constantPoolLength Some instructions might extend the constant pool, and in such
	 *            situations the length of the constant pool shall be extended.
	 */
	public void registerDependencies(LinkModel linkModel, int referencingClassId,
			MethodInClass mic, IntInABox constantPoolLength) {
		BuildinDependency[] excDeps = canThrow();
		if (excDeps != null) {
			for (BuildinDependency dep : excDeps) {
				if (dep == BuildinDependency.OUT_OF_MEMORY_ERROR_CONSTRUCTOR) {
					mic.addDependency(BuildinDependency.OUT_OF_MEMORY_ERROR_CONSTRUCTOR, linkModel,
							referencingClassId, constantPoolLength);
					mic.addDependency(BuildinDependency.OUT_OF_MEMORY_ERROR_GET_INSTANCE,
							linkModel, referencingClassId, constantPoolLength);
				} else {
					mic.addDependency(dep, linkModel, referencingClassId, constantPoolLength);
				}
			}
		}
	}

	/**
	 * This method shall be overloaded when an instruction might throw an exception
	 * 
	 * @return Per default null is returned which will be interpreted as no exceptions. When
	 *         non-null is returned this is interpreted as an array of exceptions that the
	 *         instruction might throw during run time.
	 */
	protected BuildinDependency[] canThrow() {
		return null;
	}

	/**
	 * This method handles a reference to a member. 'handles' means that when this method returns
	 * the class has been added to the link model; the referenced member has been marked as a
	 * dependency; and if the class or method hasn't been referenced before its dependencies are
	 * handled in the same manner (using recursion).
	 * 
	 * @param referencedClass The class to reference to
	 * @param memberName The name of the member to reference
	 * @param signature The signature of the referenced member
	 * @param referencingClassId The class id of the class referencing the member
	 * @param constantPoolIndex Reference to the constant pool index. When creating a member
	 *            reference, the number of elements in the constant pool is increased by one through
	 *            this reference.
	 * @return The created member reference
	 */
	protected MemberReference createMemberReference(LinkModel linkModel, String referencedClass,
			String memberName, String signature, int referencingClassId, IntInABox constantPoolIndex) {
		return linkModel.createMemberReference(referencedClass,
				new Signature(memberName, signature), referencingClassId,
				constantPoolIndex.increment());
	}

	/**
	 * This method returns the opcode for an instruction identified by the defining class
	 * 
	 * @param clazz Identifies the instruction to lookup
	 * @return The corresponding opcode. In case of error this method will call System.exit()
	 */
	public static int getOpcode(Class<? extends AbstractInstruction> clazz) {
		Integer res = null;
		for (Entry<Integer, Class<? extends AbstractInstruction>> entry : aInstructions.entrySet()) {
			if (entry.getValue() == clazz) {
				res = entry.getKey();
				break;
			}
		}

		if (res == null) {
			System.err.println("No instruction matches class: " + clazz.getName());
			System.exit(1);
		}

		return res;
	}

	public static AbstractInstruction readFrom(ByteArrayInputStream in) throws IOException {
		int opcode = in.read();
		if (opcode < 0) {
			throw new EOFException();
		}

		AbstractInstruction ins = lookup(opcode);
		if (ins == null) {
			throw new IOException("No instruction defined for opcode: "
					+ String.format("0x%02x", opcode));
		}
		ins.aCode = new byte[ins.aSize];
		ins.aCode[0] = (byte) opcode;
		for (int i = 1; i < ins.aSize; i++) {
			int code = in.read();
			if (code < 0) {
				throw new EOFException();
			}
			ins.aCode[i] = (byte) code;
		}

		return ins;
	}

	/**
	 * This method returns the Constant Pool reference positioned at position 'offset' within this
	 * instruction
	 * 
	 * @param offset The offset from where the most significant byte of the reference is placed
	 * @return The two-byte value as an int
	 */
	protected int getReference(int offset) {
		int res = ((int) aCode[offset]) & 0xff;
		res += ((int) aCode[offset + 1]) & 0xff;
		return res;
	}

	/**
	 * This method returns the Constant Pool reference positioned at position 'offset' within this
	 * instruction
	 * 
	 * @param offset The offset from where the one and only byte of the reference is placed
	 * @return The one-byte value as an int
	 */
	protected int getOneByteReference(int offset) {
		int res = ((int) aCode[offset]) & 0xff;
		return res;
	}

	/**
	 * This method generates C-code for this instruction and print to the supplied stream
	 * 
	 * @param out The stream to receive the generated code
	 */
	public void generateCode(PrintStream out) {
		out.print(BYTECODE_PREFIX + getShortForm() + "(");
		for (int i = 1; i < aCode.length; i++) {
			if (i > 1) {
				out.print(", ");
			}
			out.print(String.format("0x%02x", aCode[i]));
		}
		out.println(")");
	}

	/**
	 * This method will dump byte code macro declarations to the printstream 'out'
	 * 
	 * @param out the stream to print to
	 */
	public static void generateDeclarations(PrintStream out) {
		int implCount = 0;
		for (Integer newOpcode : aReverseInstructionMapping.keySet()) {
			int oldOpcode = aReverseInstructionMapping.get(newOpcode);

			AbstractInstruction ins = lookup(oldOpcode);
			implCount++;

			out.print("#define " + BYTECODE_PREFIX + ins.getShortForm() + "(");
			for (int i = 1; i < ins.aSize; i++) {
				if (i > 1) {
					out.print(", ");
				}
				out.print("p" + i);
			}
			out.print(") ");
			out.print(String.format("0x%02x", newOpcode));
			for (int i = 1; i < ins.aSize; i++) {
				out.print(", ");
				out.print("p" + i);
			}
			out.println(",");
		}
		// } else {
		// out.println("// " + String.format(" 0x%02x: Not implemented", opcode));
		out.println();
		out.println("// The number of instructions necessary for running this suite:");
		out.println("const size_t numberOfDefinedInstructions = " + implCount + ";");
		out.println();
		out.println("// The total number of instructions implemented by thinJ so far: "
				+ aInstructions.size());
	}

	/**
	 * This method will print instruction table declarations to the printstream 'out'
	 * 
	 * @param out the stream to print to
	 */
	public static void generateInstructionTable(PrintStream out) {
		out.println("const insWithOpcode const allInstructions[] = {");
		for (Integer newOpcode : aReverseInstructionMapping.keySet()) {
			int oldOpcode = aReverseInstructionMapping.get(newOpcode);
			AbstractInstruction ins = lookup(oldOpcode);
			if (ins != null) {
				out.println(String.format("    {%-18s 0x%02x, %-18s %d},",
						C_FUNCTION_PREFIX + ins.getShortForm() + ",", newOpcode,
						"\"" + ins.getShortForm() + "\",", ins.aSize));
				// } else {
				// out.println(String.format("    {%-18s 0x%02x, %-18s %d},", "f_unused,", opcode,
				// "\"unused\",", 1));
			}
		}
		out.println("};");

		out.println("// For testing simpler array lookup:");
		out.println("const instruction const allIns[] = {");
		for (Integer newOpcode : aReverseInstructionMapping.keySet()) {
			int oldOpcode = aReverseInstructionMapping.get(newOpcode);
			AbstractInstruction ins = lookup(oldOpcode);
			if (ins != null) {
				out.println("    " + C_FUNCTION_PREFIX + ins.getShortForm() + ",");
				// } else {
				// out.println(String.format("    {%-18s 0x%02x, %-18s %d},", "f_unused,", opcode,
				// "\"unused\",", 1));
			}
		}
		out.println("};");

	}

	/**
	 * This method will print c-function forward declarations to the printstream 'out'
	 * 
	 * @param out the stream to print to
	 */
	public static void generateForwardDeclarations(PrintStream out) {
		for (Integer newOpcode : aReverseInstructionMapping.keySet()) {
			int oldOpcode = aReverseInstructionMapping.get(newOpcode);
			AbstractInstruction ins = lookup(oldOpcode);
			if (ins != null) {
				out.println("void " + C_FUNCTION_PREFIX + ins.getShortForm() + "(void);");
			}
		}

	}

	/**
	 * This method returns the short form of the instruction
	 * 
	 * @return
	 */
	public String getShortForm() {
		return getClass().getName().replaceAll("^.*I_", "");
	}

	/**
	 * This method traverses through the 'code' byte array and de-serialises all instructions in it.
	 * For each instruction the 'handler' is called using the instructions address and the
	 * de-serialised instruction as arguments.
	 * 
	 * @param address The address of the first instruction in 'code'
	 * @param code The byte[] containing the instructions
	 * @param handler The call back handler
	 */
	public static void disassemble(int address, byte[] code, InstructionHandler handler) {
		ByteArrayInputStream bais = new ByteArrayInputStream(code);
		while (bais.available() > 0) {
			try {
				int addr = address + code.length - bais.available();

				AbstractInstruction ins = AbstractInstruction.readFrom(bais);
				handler.handle(addr, ins);
			} catch (IOException e) {
				System.err.println("Uncaught exception");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	/**
	 * This method changes all instructions that references a member. After this method the
	 * instructions will refer to the optimised set of member. Then indexing can be used instead of
	 * searching in the set of member references
	 * 
	 * @param classId The id of the class containing the code
	 * @param code The code to transform
	 * @return The code using the optimised member references only.
	 */
	public static byte[] renumberMemberReferences(int classId, byte[] code){
		ByteArrayInputStream bais = new ByteArrayInputStream(code);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (bais.available() > 0) {
			try {
				AbstractInstruction ins = AbstractInstruction.readFrom(bais);
				ins.renumberReference(classId, baos);
			} catch (IOException e) {
				System.err.println("Uncaught exception");
				e.printStackTrace();
				System.exit(1);
			}
		}

		return baos.toByteArray();
	}

	/**
	 * If this instruction references any member the reference shall be changed to the optimised
	 * reference from LinkModel
	 * 
	 * @param classId The class containing the code
	 * @param baos The stream to write the new instruction to
	 * @throws IOException
	 */
	protected void renumberReference(int classId, ByteArrayOutputStream baos) throws IOException {
		baos.write(aCode);
	}

	/**
	 * This method converts an opcode to a new instance of the corresponding AbstractInstruction
	 * 
	 * @param opcode The opcode to convert
	 * @return null is returned if no match
	 */
	public static AbstractInstruction lookup(int opcode) {
		Class<? extends AbstractInstruction> cl = aInstructions.get(opcode);
		AbstractInstruction ins = null;
		if (cl != null) {
			try {
				ins = cl.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		return ins;
	}

	public static class I_nop extends AbstractInstruction {
		public I_nop() {
			super(1);
		}
	}

	public static class I_aconst_null extends AbstractInstruction {
		public I_aconst_null() {
			super(1);
		}
	}

	public static class I_iconst_m1 extends AbstractInstruction {
		public I_iconst_m1() {
			super(1);
		}
	}

	public static class I_iconst_0 extends AbstractInstruction {
		public I_iconst_0() {
			super(1);
		}
	}

	public static class I_iconst_1 extends AbstractInstruction {
		public I_iconst_1() {
			super(1);
		}
	}

	public static class I_iconst_2 extends AbstractInstruction {
		public I_iconst_2() {
			super(1);
		}
	}

	public static class I_iconst_3 extends AbstractInstruction {
		public I_iconst_3() {
			super(1);
		}
	}

	public static class I_iconst_4 extends AbstractInstruction {
		public I_iconst_4() {
			super(1);
		}
	}

	public static class I_iconst_5 extends AbstractInstruction {
		public I_iconst_5() {
			super(1);
		}
	}

	public static class I_bipush extends AbstractInstruction {
		public I_bipush() {
			super(2);
		}
	}

	public static class I_sipush extends AbstractInstruction {
		public I_sipush() {
			super(3);
		}
	}

	public static class I_ldc extends AbstractInstruction {
		public I_ldc() {
			super(2);
		}

		@Override
		public void registerDependencies(LinkModel linkModel, int referencingClassId,
				MethodInClass mic, IntInABox constantPoolLength) {
			super.registerDependencies(linkModel, referencingClassId, mic, constantPoolLength);
			// If the referenced item is a class ref this shall be marked as a dependency:
			int cpIx = getOneByteReference(1);
			boolean found = false;

			if (!found) {
				ClassReference[] crefa = linkModel.getAllClassReferences();
				for (ClassReference classRef : crefa) {
					if (classRef.getClassId() == referencingClassId
							&& classRef.getConstantPoolIndex() == cpIx) {
						mic.addClassDependency(classRef);
						found = true;
					}
				}
			}

			if (!found) {
				found = findConstantReference(linkModel.getAllStringConstantReferences(),
						referencingClassId, cpIx, mic);
			}
			if (!found) {
				found = findConstantReference(linkModel.getAllDoubleConstantReferences(),
						referencingClassId, cpIx, mic);
			}
			if (!found) {
				found = findConstantReference(linkModel.getAllFloatConstantReferences(),
						referencingClassId, cpIx, mic);
			}
			if (!found) {
				found = findConstantReference(linkModel.getAllIntegerConstantReferences(),
						referencingClassId, cpIx, mic);
			}
			if (!found) {
				found = findConstantReference(linkModel.getAllLongConstantReferences(),
						referencingClassId, cpIx, mic);
			}
		}

		/**
		 * This method searches for a constant reference in the list of constant references and if
		 * found, the constant reference is added as a dependency to the method
		 * 
		 * @param list The list to search in
		 * @param referencingClassId The class referencing
		 * @param cpIx The constant pool index
		 * @param mic The method
		 * @return true, if found; false if not
		 */
		private <T> boolean findConstantReference(List<ConstantReference<T>> list,
				int referencingClassId, int cpIx, MethodInClass mic) {
			boolean found = false;
			for (ConstantReference<T> cref : list) {
				if (cref.getClassId() == referencingClassId && cref.getConstantPoolIndex() == cpIx) {
					mic.addConstantReference(cref);
					found = true;
				}
			}

			return found;
		}
	}

	public static class I_iload extends AbstractInstruction {
		public I_iload() {
			super(2);
		}
	}

	public static class I_aload extends AbstractInstruction {
		public I_aload() {
			super(2);
		}
	}

	public static class I_iload_0 extends AbstractInstruction {
		public I_iload_0() {
			super(1);
		}
	}

	public static class I_iload_1 extends AbstractInstruction {
		public I_iload_1() {
			super(1);
		}
	}

	public static class I_iload_2 extends AbstractInstruction {
		public I_iload_2() {
			super(1);
		}
	}

	public static class I_iload_3 extends AbstractInstruction {
		public I_iload_3() {
			super(1);
		}
	}

	public static class I_aload_0 extends AbstractInstruction {
		public I_aload_0() {
			super(1);
		}
	}

	public static class I_aload_1 extends AbstractInstruction {
		public I_aload_1() {
			super(1);
		}
	}

	public static class I_aload_2 extends AbstractInstruction {
		public I_aload_2() {
			super(1);
		}
	}

	public static class I_aload_3 extends AbstractInstruction {
		public I_aload_3() {
			super(1);
		}
	}

	public static class I_iaload extends AbstractInstruction {
		public I_iaload() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR,
					BuildinDependency.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_CONSTRUCTOR };
		}

	}

	public static class I_aaload extends AbstractInstruction {
		public I_aaload() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR,
					BuildinDependency.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_baload extends AbstractInstruction {
		public I_baload() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR,
					BuildinDependency.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_caload extends AbstractInstruction {
		public I_caload() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR,
					BuildinDependency.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_istore extends AbstractInstruction {
		public I_istore() {
			super(2);
		}
	}

	public static class I_astore extends AbstractInstruction {
		public I_astore() {
			super(2);
		}
	}

	public static class I_istore_0 extends AbstractInstruction {
		public I_istore_0() {
			super(1);
		}
	}

	public static class I_istore_1 extends AbstractInstruction {
		public I_istore_1() {
			super(1);
		}
	}

	public static class I_istore_2 extends AbstractInstruction {
		public I_istore_2() {
			super(1);
		}
	}

	public static class I_istore_3 extends AbstractInstruction {
		public I_istore_3() {
			super(1);
		}
	}

	public static class I_astore_0 extends AbstractInstruction {
		public I_astore_0() {
			super(1);
		}
	}

	public static class I_astore_1 extends AbstractInstruction {
		public I_astore_1() {
			super(1);
		}
	}

	public static class I_astore_2 extends AbstractInstruction {
		public I_astore_2() {
			super(1);
		}
	}

	public static class I_astore_3 extends AbstractInstruction {
		public I_astore_3() {
			super(1);
		}
	}

	public static class I_iastore extends AbstractInstruction {
		public I_iastore() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR,
					BuildinDependency.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_aastore extends AbstractInstruction {
		public I_aastore() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR,
					BuildinDependency.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_bastore extends AbstractInstruction {
		public I_bastore() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR,
					BuildinDependency.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_castore extends AbstractInstruction {
		public I_castore() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR,
					BuildinDependency.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_pop extends AbstractInstruction {
		public I_pop() {
			super(1);
		}
	}

	public static class I_dup extends AbstractInstruction {
		public I_dup() {
			super(1);
		}
	}

	public static class I_dup2 extends AbstractInstruction {
		public I_dup2() {
			super(1);
		}
	}

	public static class I_iadd extends AbstractInstruction {
		public I_iadd() {
			super(1);
		}
	}

	public static class I_isub extends AbstractInstruction {
		public I_isub() {
			super(1);
		}
	}

	public static class I_imul extends AbstractInstruction {
		public I_imul() {
			super(1);
		}
	}

	public static class I_idiv extends AbstractInstruction {
		public I_idiv() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.ARITHMETIC_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_irem extends AbstractInstruction {
		public I_irem() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.ARITHMETIC_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_iand extends AbstractInstruction {
		public I_iand() {
			super(1);
		}
	}

	public static class I_ior extends AbstractInstruction {
		public I_ior() {
			super(1);
		}
	}

	public static class I_ixor extends AbstractInstruction {
		public I_ixor() {
			super(1);
		}
	}

	public static class I_iinc extends AbstractInstruction {
		public I_iinc() {
			super(3);
		}
	}

	public static class I_i2b extends AbstractInstruction {
		public I_i2b() {
			super(1);
		}
	}

	public static class I_ifeq extends AbstractInstruction {
		public I_ifeq() {
			super(3);
		}
	}

	public static class I_ifne extends AbstractInstruction {
		public I_ifne() {
			super(3);
		}
	}

	public static class I_ifgt extends AbstractInstruction {
		public I_ifgt() {
			super(3);
		}
	}

	public static class I_ifle extends AbstractInstruction {
		public I_ifle() {
			super(3);
		}
	}

	public static class I_iflt extends AbstractInstruction {
		public I_iflt() {
			super(3);
		}
	}

	public static class I_ifge extends AbstractInstruction {
		public I_ifge() {
			super(3);
		}
	}

	public static class I_if_icmpeq extends AbstractInstruction {
		public I_if_icmpeq() {
			super(3);
		}
	}

	public static class I_if_icmpne extends AbstractInstruction {
		public I_if_icmpne() {
			super(3);
		}
	}

	public static class I_if_icmplt extends AbstractInstruction {
		public I_if_icmplt() {
			super(3);
		}
	}

	public static class I_if_icmpge extends AbstractInstruction {
		public I_if_icmpge() {
			super(3);
		}
	}

	public static class I_if_icmpgt extends AbstractInstruction {
		public I_if_icmpgt() {
			super(3);
		}
	}

	public static class I_if_icmple extends AbstractInstruction {
		public I_if_icmple() {
			super(3);
		}
	}

	public static class I_if_acmpeq extends AbstractInstruction {
		public I_if_acmpeq() {
			super(3);
		}
	}

	public static class I_if_acmpne extends AbstractInstruction {
		public I_if_acmpne() {
			super(3);
		}
	}

	public static class I_goto extends AbstractInstruction {
		public I_goto() {
			super(3);
		}
	}

	public static class I_ireturn extends AbstractInstruction {
		public I_ireturn() {
			super(1);
		}
		// TODO throws IllegalMonitorStateException
	}

	public static class I_areturn extends AbstractInstruction {
		public I_areturn() {
			super(1);
		}
		// TODO throws IllegalMonitorStateException
	}

	public static class I_vreturn extends AbstractInstruction {
		public I_vreturn() {
			super(1);
		}
		// TODO throws IllegalMonitorStateException
	}

	public static abstract class FieldReferencing extends AbstractInstruction {
		public FieldReferencing() {
			super(3);
		}

		@Override
		public void registerDependencies(LinkModel linkModel, int referencingClassId,
				MethodInClass mic, IntInABox constantPoolLength) {
			// GETFIELD, GETSTATIC, PUTFIELD, PUTSTATIC
			super.registerDependencies(linkModel, referencingClassId, mic, constantPoolLength);
			MemberReference mref = linkModel
					.getMemberReference(referencingClassId, getReference(1));
			mic.addReference(mref);
		}
	}

	public static class I_getstatic extends FieldReferencing {
	}

	public static class I_putstatic extends FieldReferencing {
	}

	public static class I_getfield extends FieldReferencing {
		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_putfield extends FieldReferencing {
		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static abstract class MethodReferencing extends AbstractInstruction {
		public MethodReferencing(int length) {
			super(length);
		}

		@Override
		public void registerDependencies(LinkModel linkModel, int referencingClassId,
				MethodInClass mic, IntInABox constantPoolLength) {
			super.registerDependencies(linkModel, referencingClassId, mic, constantPoolLength);
			// INVOKEINTERFACE, INVOKESPECIAL, INVOKESTATIC, INVOKEVIRTUAL
			MemberReference mref = linkModel
					.getMemberReference(referencingClassId, getReference(1));
			mic.addReference(mref);
		}

		@Override
		protected void renumberReference(int referencingClassId, ByteArrayOutputStream baos) {
			int constantPoolIndex = getReference(1);
			MemberReference mref = LinkModel.getInstance().getOptimizedReference(
					referencingClassId, constantPoolIndex);
			baos.write(aCode[0]);
			baos.write(mref.getConstantPoolIndex() >> 8);
			baos.write(mref.getConstantPoolIndex() & 0xff);
			for (int i = 3; i < aCode.length; i++) {
				baos.write(aCode[i]);
			}
		}
	}

	public static class I_invokevirtual extends MethodReferencing {
		public I_invokevirtual() {
			super(3);
		}
	}

	public static class I_invokespecial extends MethodReferencing {
		public I_invokespecial() {
			super(3);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_invokestatic extends MethodReferencing {
		public I_invokestatic() {
			super(3);
		}
	}

	public static class I_invokeinterface extends MethodReferencing {
		public I_invokeinterface() {
			super(5);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_unused extends AbstractInstruction {
		public I_unused() {
			super(1);
		}
	}

	public static class I_new extends AbstractInstruction {
		public I_new() {
			super(3);
		}

		@Override
		public void registerDependencies(LinkModel linkModel, int referencingClassId,
				MethodInClass mic, IntInABox constantPoolLength) {
			super.registerDependencies(linkModel, referencingClassId, mic, constantPoolLength);
			// Add dependency to element class:
			ClassReference classReference = linkModel.getClassReference(referencingClassId,
					getReference(1));
			mic.addClassDependency(classReference);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.OUT_OF_MEMORY_ERROR_CONSTRUCTOR };
		}
	}

	public static class I_newarray extends AbstractInstruction {
		public I_newarray() {
			super(2);
		}

		/**
		 * This method returns the array type. If unable to read a legal array type, this method
		 * will not return
		 * 
		 * @return The array type
		 */
		private ClassTypeEnum getType() {
			ClassTypeEnum type = ClassTypeEnum.resolveByArrayType(aCode[1]);
			if (type == null || type == ClassTypeEnum.InterfaceType
					|| type == ClassTypeEnum.ClassType) {
				System.err.println("Illegal array type value: " + aCode[1]);
				System.exit(1);
			}
			return type;
		}

		@Override
		public void registerDependencies(LinkModel linkModel, int referencingClassId,
				MethodInClass mic, IntInABox constantPoolLength) {
			super.registerDependencies(linkModel, referencingClassId, mic, constantPoolLength);
			ClassTypeEnum type = getType();
			mic.addSimpleArrayDependency(type);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NEGATIVE_ARRAY_SIZE_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_anewarray extends AbstractInstruction {
		public I_anewarray() {
			super(3);
		}

		@Override
		public void registerDependencies(LinkModel linkModel, int referencingClassId,
				MethodInClass mic, IntInABox constantPoolLength) {
			super.registerDependencies(linkModel, referencingClassId, mic, constantPoolLength);
			// Add dependency to element class:
			ClassReference classReference = linkModel.getClassReference(referencingClassId,
					getReference(1));
			mic.addClassDependency(classReference);
			// Add dependency to array class:
			classReference = linkModel.createClassReference(referencingClassId,
					constantPoolLength.increment(), "[L" + classReference.getClassName());
			mic.addClassDependency(classReference);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NEGATIVE_ARRAY_SIZE_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_arraylength extends AbstractInstruction {
		public I_arraylength() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR };
		}
	}

	public abstract static class AbstractCheckcast extends AbstractInstruction {
		public AbstractCheckcast() {
			super(3);
		}

		@Override
		public void registerDependencies(LinkModel linkModel, int referencingClassId,
				MethodInClass mic, IntInABox constantPoolLength) {
			super.registerDependencies(linkModel, referencingClassId, mic, constantPoolLength);
			ClassReference cref = linkModel.getClassReference(referencingClassId, getReference(1));
			mic.addClassDependency(cref);
		}
	}

	public static class I_checkcast extends AbstractCheckcast {
		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.CLASS_CAST_EXCEPTION_CONSTRUCTOR };
		}
	}

	public static class I_instanceof extends AbstractCheckcast {
	}

	public static class I_ifnull extends AbstractInstruction {
		public I_ifnull() {
			super(3);
		}
	}

	public static class I_ifnonnull extends AbstractInstruction {
		public I_ifnonnull() {
			super(3);
		}
	}

	public static class I_ineg extends AbstractInstruction {
		public I_ineg() {
			super(1);
		}
	}

	public static class I_ishl extends AbstractInstruction {
		public I_ishl() {
			super(1);
		}
	}

	public static class I_ishr extends AbstractInstruction {
		public I_ishr() {
			super(1);
		}
	}

	public static class I_athrow extends AbstractInstruction {
		public I_athrow() {
			super(1);
		}

		@Override
		protected BuildinDependency[] canThrow() {
			return new BuildinDependency[] { BuildinDependency.NULL_POINTER_EXCEPTION_CONSTRUCTOR };
		}
	}

	// public static class I_monitorenter extends AbstractInstruction {
	// public I_monitorenter() {
	// super(1);
	// }
	// }
	//
	// public static class I_monitorexit extends AbstractInstruction {
	// public I_monitorexit() {
	// super(1);
	// }
	// }

	/**
	 * This method registers that this type of instruction shall be included in the suite
	 */
	public void referenced() {
		if (!aInstructionCodeMapping.containsKey((int) aCode[0] & 0xff)) {
			int newOpcode = aInstructionCodeGenerator++;
			aInstructionCodeMapping.put((int) aCode[0] & 0xff, newOpcode);
			aReverseInstructionMapping.put(newOpcode, (int) aCode[0] & 0xff);
		}
	}
}