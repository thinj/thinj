//------------------------------------------------------------------------------
// Classification: COMPANY UNCLASSIFIED
//
// Copyright (c) Terma A/S
// All rights reserved
//------------------------------------------------------------------------------

package thinj.regression;

public class StaticInstanceAndMethod {
	static int aSpade;
	static final int aDims = Integer.MIN_VALUE;
	static int aFoobar = 4711;
	static boolean aBool;

	static {
		//TinySystem.outInt(83456856);
	}
	static {
		//TinySystem.outInt(-1);
	}

	static int faculty(int n) {
		boolean theEnd = n == 0;
		if (theEnd) {
			Regression.verify(theEnd);
			return 1;
		} else {
			int spade = n * faculty(n - 1);
			return spade;
		}
	}

	private static void ThisTestShallFail() {
		Regression.verify(false);
	}

	private static void test1() {
		Regression.verify(aFoobar > aDims);
	}

	private static int test2() {
		return 7;
	}

	// Note! No args to main!
	public static void main() {
		ThisTestShallFail();
		int result = faculty(7);
		Regression.verify(result == 5040);
		test1();
		Regression.verify(5 + test2() == 12);
		// TinySystem.outInt(345);
		// new Object();
		// new DavsDu();
	}

	static {
		// TinySystem.outInt(78);
	}
}
