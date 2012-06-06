package thinj.regression.arrays;

public class DummyClass {
	static {
		//System.out.println("1919191");
	}
	private int x;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public DummyClass() {
		x = 47;
	}

	public static void cyclicDependency() {
		// Uncomment this one to test cyclic dependency check:
		//MultiArrayTest.test3();
	}

}
