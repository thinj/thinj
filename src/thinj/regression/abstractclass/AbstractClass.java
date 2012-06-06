package thinj.regression.abstractclass;

public abstract class AbstractClass {
	protected int aValue;

	protected AbstractClass(int x) {
		aValue = x;
	}
	
	public abstract void foobar();

	private static void test1() {
		AbstractClass ac = new ExtendingClass(45);
		ac.foobar();
	}

	private static void test2() {
		ExtendingClass ac = new ExtendingClass(45);
		ac.foobar();
	}

	public static void main() {
		test1();
		test2();
	}
}
