package thinj.linkmodel;

/**
 * This class represents a 'Signature'. A signature consists of a name and a Description
 * (see section 4.3 in
 * http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#1513)
 * 
 * @author hammer
 * 
 */
public class Signature implements Comparable<Signature> {
	private final String aName;
	private final String aDescriptor;
	private final boolean aMethod;

	/**
	 * 
	 * @param name The name of the method
	 * @param descriptor
	 */
	public Signature(String name, String descriptor) {
		aName = name;
		aDescriptor = descriptor;
		aMethod = aDescriptor.charAt(0) == '(';
	}

	public boolean isMethod() {
		return aMethod;
	}

	public String getName() {
		return aName;
	}

	public String getDescriptor() {
		return aDescriptor;
	}

	@Override
	public String toString() {
		return "Signature [" + (aMethod ? "Method" : "Field") 
		        +", aName=" + aName 
				+ ", aDescriptor=" + aDescriptor  
				+ "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aName == null) ? 0 : aName.hashCode());
		result = prime * result + ((aDescriptor == null) ? 0 : aDescriptor.hashCode());
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
		Signature other = (Signature) obj;
		if (aName == null) {
			if (other.aName != null)
				return false;
		} else if (!aName.equals(other.aName))
			return false;
		if (aDescriptor == null) {
			if (other.aDescriptor != null)
				return false;
		} else if (!aDescriptor.equals(other.aDescriptor))
			return false;
		return true;
	}

	@Override
	public int compareTo(Signature that) {
		int res = this.aName.compareTo(that.aName);
		if (res == 0) {
			res = this.aDescriptor.compareTo(that.aDescriptor);
		}
		return res;
	}

	public String format() {
		return aName + "#" + aDescriptor;
	}
}
