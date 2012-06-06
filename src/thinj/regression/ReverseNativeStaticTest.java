package thinj.regression;

/**
 * This class tests calling static native method from java, and from native calling a static java
 * method.<br/>
 * <br/>
 * Note! Will only work if the map.h - file is correctly copied to the c-source tree. <br/>
 * 
 */
public class ReverseNativeStaticTest {
	private static int aBarVisited = 0;

	public native static void foo();

	public static void bar() {
		aBarVisited++;
	}

	public static void main() {
		bar();
		foo();
		Regression.verify(aBarVisited == 2);
	}
}
