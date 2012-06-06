package thinj.tables;

import java.util.LinkedList;

public class ImplementedInterfaces {
	// The name of the class implementing the interfaces:
	public String implementingClassName;

	// The list of implemented interfaces:
	public LinkedList<Integer> classIds;

	public ImplementedInterfaces(String implementingClassName) {
		super();
		this.implementingClassName = implementingClassName;
		this.classIds = new LinkedList<Integer>();
	}
}
