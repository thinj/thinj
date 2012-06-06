package thinj.regression.exceptions;

import thinj.regression.Regression;

public class ExIALOAD {
	private int x;

	private int get(int i) {
		return 67;
	}

	private int get() {
		return 0;
	}

	private static void iaload() {
		// ---------------------------------
		int[] ia = null;

		boolean caught = false;
		try {
			int x = ia[1];
		} catch (NullPointerException e) {
			caught = true;
		}

		Regression.verify(caught);
		// ---------------------------------
		ia = new int[1];
		ia[0] = 45;

		caught = false;
		try {
			int x = ia[2];
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		}
		Regression.verify(caught);
		// ---------------------------------
	}

	private static void aaload() {
		// ---------------------------------
		Object[] oa = null;

		boolean caught = false;
		try {
			Object x = oa[1];
		} catch (NullPointerException e) {
			caught = true;
		}

		Regression.verify(caught);
		// ---------------------------------
		oa = new Object[1];
		oa[0] = new Object();

		caught = false;
		try {
			Object x = oa[2];
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		}
		Regression.verify(caught);
		// ---------------------------------
	}

	private static void baload() {
		// ---------------------------------
		byte[] oa = null;

		boolean caught = false;
		try {
			byte x = oa[1];
		} catch (NullPointerException e) {
			caught = true;
		}

		Regression.verify(caught);
		// ---------------------------------
		oa = new byte[1];
		oa[0] = 17;

		caught = false;
		try {
			byte x = oa[2];
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		}
		Regression.verify(caught);
		// ---------------------------------
	}

	private static void caload() {
		// ---------------------------------
		char[] oa = null;

		boolean caught = false;
		try {
			char x = oa[1];
		} catch (NullPointerException e) {
			caught = true;
		}

		Regression.verify(caught);
		// ---------------------------------
		oa = new char[1];
		oa[0] = 17;

		caught = false;
		try {
			char x = oa[2];
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		}
		Regression.verify(caught);
		// ---------------------------------
	}

	private static void iastore() {
		// ---------------------------------
		int[] ia = null;

		boolean caught = false;
		try {
			ia[1] = 45;
		} catch (NullPointerException e) {
			caught = true;
		}

		Regression.verify(caught);
		// ---------------------------------
		ia = new int[1];
		ia[0] = 45;

		caught = false;
		try {
			ia[2] = 19;
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		}
		Regression.verify(caught);
		// ---------------------------------
	}

	private static void aastore() {
		// ---------------------------------
		Object[] ia = null;

		boolean caught = false;
		try {
			ia[1] = new Object();
		} catch (NullPointerException e) {
			caught = true;
		}

		Regression.verify(caught);
		// ---------------------------------
		ia = new Object[1];
		ia[0] = new Object();

		caught = false;
		try {
			ia[2] = new Object();
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		}
		Regression.verify(caught);
		// ---------------------------------
	}

	private static void bastore() {
		// ---------------------------------
		boolean[] ia = null;

		boolean caught = false;
		try {
			ia[1] = false;
		} catch (NullPointerException e) {
			caught = true;
		}

		Regression.verify(caught);
		// ---------------------------------
		ia = new boolean[1];
		ia[0] = true;

		caught = false;
		try {
			ia[2] = false;
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		}
		Regression.verify(caught);
		// ---------------------------------
	}

	private static void castore() {
		// ---------------------------------
		char[] ia = null;

		boolean caught = false;
		try {
			ia[1] = 'a';
		} catch (NullPointerException e) {
			caught = true;
		}

		Regression.verify(caught);
		// ---------------------------------
		ia = new char[1];
		ia[0] = 'A';

		caught = false;
		try {
			ia[2] = '@';
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		}
		Regression.verify(caught);
		// ---------------------------------
	}

	private static void idiv() {
		boolean caught = false;
		int x = 89;
		int y = 0;
		try {
			int z = x / y;
		} catch (ArithmeticException e) {
			caught = true;
		}
		Regression.verify(caught);
	}

	private static void irem() {
		boolean caught = false;
		int x = 89;
		int y = 0;
		try {
			int z = x % y;
		} catch (ArithmeticException e) {
			caught = true;
		}
		Regression.verify(caught);
	}

	private static void getfield() {
		ExIALOAD obj = null;
		boolean caught = false;
		try {
			int i = obj.x;
		} catch (NullPointerException e) {
			caught = true;
		}
		Regression.verify(caught);
	}

	private static void putfield() {
		ExIALOAD obj = null;
		boolean caught = false;
		try {
			obj.x = 78;
		} catch (NullPointerException e) {
			caught = true;
		}
		Regression.verify(caught);
	}

	private static void invokespecial() {
		ExIALOAD obj = null;
		boolean caught = false;
		try {
			int i = obj.get(56);
		} catch (NullPointerException e) {
			caught = true;
		}
		Regression.verify(caught);

		caught = false;
		try {
			int i = obj.get();
		} catch (NullPointerException e) {
			caught = true;
		}
		Regression.verify(caught);
	}

	private static void invokevirtual() {
		ExIALOAD obj = null;
		boolean caught = false;
		try {
			String s = obj.toString();
		} catch (NullPointerException e) {
			caught = true;
		}
		Regression.verify(caught);
	}

	private interface MyInterface {
		void foobar();
	}

	private static void invokeinterface() {
		MyInterface obj = null;
		boolean caught = false;
		try {
			obj.foobar();
		} catch (NullPointerException e) {
			caught = true;
		}
		Regression.verify(caught);
	}


	// Very simple linked list:
	private static class LinkObject {
		private LinkObject aNext;
		private int[] aPayload;

		public LinkObject(LinkObject next, int size) {
			aNext = next;
			if (size > 0) {
				aPayload = new int[size];
			}
		}
		
		public static void consumeHeap(int size) {
			LinkObject l = null;
			
			while (true) {
				l = new LinkObject(l, 0);
			}
		}
	}

	private static void test_new() {
		boolean caught = false;
		try {
			LinkObject.consumeHeap(0);
		} catch (OutOfMemoryError e) {
			caught = true;
			//System.out.println("out of mem!");
		}
		Regression.verify(caught);
	}
	
	private static void newarray() {
		boolean caught = false;
		try {
			int[] ia = new int[-3];
		} catch (NegativeArraySizeException e) {
			caught = true;
		}
		
		Regression.verify(caught);
	}
	

	private static void anewarray() {
		boolean caught = false;
		try {
			String[] sa = new String[-3];
		} catch (NegativeArraySizeException e) {
			caught = true;
		}
		
		Regression.verify(caught);
	}
	

	private static void arraylength() {
		boolean caught = false;
		String[] sa = null;
		try {
			int i = sa.length;
		} catch (NullPointerException e) {
			caught = true;
		}
		
		Regression.verify(caught);
	}
	

	private static void athrow() {
		boolean caught = false;
		try {
			throw null;
		} catch (NullPointerException e) {
			caught = true;
		}
		
		Regression.verify(caught);
	}

	private static void checkcast() {
		boolean caught = false;
		try {
			Object x = new int[0];
			System.out.println((String) x);
		} catch (ClassCastException e) {
			caught = true;
		}
		
		Regression.verify(caught);
	}

	public static void main() {
		try {
			iaload();
			aaload();
			baload();
			caload();
			iastore();
			aastore();
			bastore();
			castore();
			idiv();
			irem();
			getfield();
			putfield();
			invokespecial();
			invokevirtual();			
			invokeinterface();
			//test_new();
			newarray();
			anewarray();			
			arraylength();			
			athrow();			
			checkcast();
		} catch (Throwable e) {
			System.out.println("Throwable xx caught: " + e);
		}
	}
}
