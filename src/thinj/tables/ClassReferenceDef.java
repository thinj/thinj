package thinj.tables;

public class ClassReferenceDef {
	// The class id of the referencing class:
	int classId;

	// Index into constant pool of the referencing class:
	int constantPoolIndex;

	// The class id of the referenced class:
	int targetClassId;

	// The name of the referenced class:
	String className;
}
