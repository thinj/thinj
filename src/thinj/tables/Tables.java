package thinj.tables;

import java.util.HashMap;
import java.util.LinkedList;

public class Tables {
	private static HashMap<Integer, ClassInstanceInfoDef> aClassInstanceInfoDefTable;
	private static LinkedList<ImplementedInterfaces> aImplementedInterfacesTable;
	static {
		aClassInstanceInfoDefTable = new HashMap<Integer, ClassInstanceInfoDef>();
	}

	/**
	 * This method adds a member of the classInstanceInfoDef table
	 * 
	 * @param def The member of the classInstanceInfoDef table to add
	 */
	public static void add(ClassInstanceInfoDef def) {
		aClassInstanceInfoDefTable.put(def.classId, def);
	}

	/**
	 * This method adds an instance of interfaces implementation list
	 * 
	 * @param implInter The item to add
	 */
	public static void add(ImplementedInterfaces implInter) {
		aImplementedInterfacesTable.add(implInter);
	}

}
