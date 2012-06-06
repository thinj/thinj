package thinj.regression;

public class InstanceMethods {
	protected void testargs(int i, boolean b) {
		Regression.verify(i == 45);
		Regression.verify(!b);
	}
	public void basePrint(int val) {
		//TinySystem.outInt(val);
	}
	
	public void basePrint2() {
		//TinySystem.outInt(1111);
	}
	
	private static void test1() {
		InstanceMethods im = new InstanceMethods();		
		im.testargs(45, false);		
	}
	
	private static void test2() {
		InstanceMethods im = new InstanceMethodsSpecial();		
		im.testargs(145, true);
	}

	private static void test3() {
		InstanceMethods im = new InstanceMethodsSpecial();		
		im.testargs(145, true);
	}

	public static void main() {
		test1();		
		test2();
		test3();

		new InstanceMethods().basePrint(100);
	}

}
