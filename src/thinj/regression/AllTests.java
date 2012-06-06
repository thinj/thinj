package thinj.regression;

import thinj.regression.abstractclass.AbstractClass;
import thinj.regression.arrays.ArrayTest;
import thinj.regression.arrays.MultiArrayTest;
import thinj.regression.arrays.Sudoku;
import thinj.regression.checkcast.CheckCast;
import thinj.regression.exceptions.ExIALOAD;
import thinj.regression.exceptions.TestNullPointerException;
import thinj.regression.exceptions.ThrowException;
import thinj.regression.gc.GC;
import thinj.regression.gc.HeapTest;
import thinj.regression.interfaces.AnInterfaceImpl;

public class AllTests {
	public static void main() {
/*
		System.out.println("Test 1");
		StaticInstanceAndMethod.main();

		System.out.println("Test 2");
		InstanceAttributes.main();

		System.out.println("Test 3");
		ObjectTest.main();

		System.out.println("Test 4");
		InstanceSubclassing.main();

		System.out.println("Test 5");
		InstanceMethods.main();

		System.out.println("Test 6");
		ExtendedBaseClass.main();

		System.out.println("Test 7");
		AbstractClass.main();

		System.out.println("Test 8");
		AnInterfaceImpl.main();

		System.out.println("Test 9");
		ArrayTest.main();

		System.out.println("Test 10");
		MultiArrayTest.main();

		System.out.println("Test 11");
		Sudoku.main();

		System.out.println("Test 12");
		GC.main();

		System.out.println("Test 13");
		EqualsTest.main();

		System.out.println("Test 14");
		NativeTest.main();

		System.out.println("Test 15");
		CharTest.main();

		System.out.println("Test 16");
		StaticInnerClassTest.main();

		System.out.println("Test 17");
		HeapTest.main();

		System.out.println("Test 18");
		CheckCast.main();

		System.out.println("Test 19");
		Ineg.main();

		System.out.println("Test 20");
		DivMod.main();

		System.out.println("Test 21");
		ByteTest.main();
*/
		System.out.println("Test 22");
		ThrowException.main();

		System.out.println("Test 23");
		TestNullPointerException.main();
		
		System.out.println("Test 24");
		ExIALOAD.main();

	}
}
