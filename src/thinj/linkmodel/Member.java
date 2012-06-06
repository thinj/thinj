package thinj.linkmodel;

/**
 * This class represents any class member.
 * 
 * @author hammer
 * 
 */
public class Member implements Comparable<Member> {
	private final String aClassName;
	private final Signature aSignature;

	/**
	 * Constructor
	 * 
	 * @param className The name of the class containing the member
	 * @param memberName The name of the member
	 * @param descriptor The descriptor of the member
	 */
	public Member(String className, String memberName, String descriptor) {
		aClassName = ClassInSuite.getGlobalName(className);	
		aSignature = new Signature(memberName, descriptor);
	}

	public String getClassName() {
		return aClassName;
	}
	

	public Signature getSignature() {
		return aSignature;
	}

	@Override
	public int compareTo(Member that) {
		int res = this.aClassName.compareTo(that.aClassName);
		if (res == 0) {
			res = this.aSignature.compareTo(that.aSignature);
		}
		return res;
	}

	/**
	 * This method builds a nice formatted presentation of this member. For debugging
	 * purpose only
	 * 
	 * @return a nice formatted presentation of this member. For debugging purpose only
	 */
	public String format() {
		return aClassName + "#" + aSignature.getName() + "#" + aSignature.getDescriptor();
	}

	
	@Override
	public String toString() {
		return "Member [aClassName=" + aClassName + ", aSignature=" + aSignature + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aClassName == null) ? 0 : aClassName.hashCode());
		result = prime * result + ((aSignature == null) ? 0 : aSignature.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Member other = (Member) obj;
		if (aClassName == null) {
			if (other.aClassName != null)
				return false;
		} else if (!aClassName.equals(other.aClassName))
			return false;
		if (aSignature == null) {
			if (other.aSignature != null)
				return false;
		} else if (!aSignature.equals(other.aSignature))
			return false;
		return true;
	}
}
