package thinj.linkmodel;

/**
 * This class contains a linkable item. Shall be base class for classes that will link to each other
 * 
 * @author hammer
 * 
 */
public abstract class Linkable {
	private int aLinkId;
	private boolean aReferenced;

	public Linkable() {
		aReferenced = false;
	}

	/**
	 * This method sets the id of the member that this reference references.
	 * 
	 * @param linkId The id of the member that this reference references.
	 */
	public void setLinkId(int linkId) {
		aLinkId = linkId;
	}

	/**
	 * This mark this instance as referenced <=> needed in the resulting suite.
	 */
	public void referenced() {
		aReferenced = true;
	}

	/**
	 * This method return the id of the member that this reference references.
	 * 
	 * @return The id of the member that this reference references.
	 */
	public int getLinkId() {
		return aLinkId;
	}

	/**
	 * This method returns true, if the link id has been set (<=> the linkable shall be included in
	 * the suite)
	 * 
	 * @return true, if the link id has been set (<=> the linkable shall be included in the suite)
	 */
	public boolean isReferenced() {
		return aReferenced;
	}
}
