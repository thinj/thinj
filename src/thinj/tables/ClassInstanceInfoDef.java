package thinj.tables;

import thinj.linkmodel.ClassTypeEnum;

public class ClassInstanceInfoDef {
	// const classInstanceInfoDef const allClassInstanceInfo[] = {
	// { 0, 0, 0, 0, 0, CT_CLASS, 0, 0 }, // java/lang/Object
	// { 5, 0, 0, 0, 0, CT_CHAR_ARRAY, 0, sizeof(jchar) }, // [C

	// The class id of the class:
	public int classId; // classId == 0 <=> it's java.lang.Object
	// The class id of the super class:
	public int superClassId; // if superClassId == 0 => it's java.lang.Object
	// The size of an instance
	public int instanceSize;
	// The index of the list of implemented interfaces:
	public int interface_start;
	// The number of implemented interfaces:
	public int interface_count;
	// The type of the class:
	public ClassTypeEnum type;
	// The class id of an array element - is only defined when type == CT_OBJECT_ARRAY:
	public int elementClassId;
	// The size of a single element in the array. Makes sense for arrays only:
	public String elementSize;
	// The name of the class:
	public String className;

	public ClassInstanceInfoDef(int classId, int superClassId, int instanceSize,
			int interface_start, int interface_count, ClassTypeEnum type, int elementClassId,
			String elementSize, String className) {
		super();
		this.classId = classId;
		this.superClassId = superClassId;
		this.instanceSize = instanceSize;
		this.interface_start = interface_start;
		this.interface_count = interface_count;
		this.type = type;
		this.elementClassId = elementClassId;
		this.elementSize = elementSize;
		this.className = className;
	}

}
