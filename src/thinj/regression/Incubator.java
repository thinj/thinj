package thinj.regression;

public class Incubator {
	private int aValue;
	private boolean aEnabled;
	
	private Object aObject;
	
	public Incubator() {
		aValue = 47;
		aEnabled = true;
		aObject = null;
	}

	
	public static void main() {
		Incubator inc = new Incubator();
		//Object o = new Object();
		Regression.verify(inc.aValue == 47);
		Regression.verify(inc.aEnabled);
		// TODO null-test
		Regression.verify(inc.aObject == null);
	}
}
