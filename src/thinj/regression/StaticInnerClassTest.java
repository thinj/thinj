package thinj.regression;


/**
 * This class contains the strange phaenomenon of an hidden, autgenerated constructor for the inner class 
 * @author hammer
 *
 */
public class StaticInnerClassTest {
	public static void main() {
	}

	public static final Dims ST = new Dims();
	private static class Dims {
	}
}
