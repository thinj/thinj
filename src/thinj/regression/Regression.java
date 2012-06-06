package thinj.regression;

/**
 * This class shall never be included in the files passed to the 'Linker'. it's only
 * purpose is to enable compilation of other java classes referencing a Regression class.
 * The real Regression class is build automatically by the 'Linker' class.
 * 
 * @author hammer
 * 
 */
public class Regression {
	public static native void verify(boolean b);
//	public static void verify(boolean b) {
//		if (!b) {
//			boolean bb = b;
//			System.err.println("Regression error");
//			new Exception().printStackTrace();			
//		}
//	}
}
