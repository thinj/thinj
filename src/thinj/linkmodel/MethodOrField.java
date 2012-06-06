package thinj.linkmodel;

/**
 * This class contains the attributes defining a field or method in a class file
 * 
 */
public abstract class MethodOrField extends Linkable {
	private final Member aMember;
	private final boolean aStatic;

	/**
	 * Constructor
	 * 
	 * @param member The identification of the method / field
	 */
	public MethodOrField(Member member, boolean isStatic) {
		aMember = member;
		aStatic = isStatic;
	}

	public boolean isStatic() {
		return aStatic;
	}

	public Member getMember() {
		return aMember;
	}
}
