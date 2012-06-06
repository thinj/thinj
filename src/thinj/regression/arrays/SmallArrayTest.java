package thinj.regression.arrays;


public class SmallArrayTest {
//	private int[] aValues;
//
//	public SmallArrayTest() {
//		aValues = new int[4];
//		aValues[0] = 45;
//		System.out.println("hello folks");
//		int i = aValues[0];
//		System.out.println("aVa[3] = " + i);
//		Regression.verify(aValues[0] == 45);
//
//	}
//
//	private static void test1() {
//		new SmallArrayTest();
//	}

	private static int[] aValues;

	public static void test1() {
		aValues = new int[4];
		aValues[0] = 45;
		int i = aValues[0];
		System.out.println("aVa[3] = " + i);

	}

	public static void main() {
		test1();
	}
}
