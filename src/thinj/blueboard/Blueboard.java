package thinj.blueboard;

public class Blueboard {
	public static native final void init();

	public static native final void outInt(int i);

	public static native final void delay(int delayInMillis);

	public static native final void outString(String s);

	public static native final void rs232OutString(String s);

	public static native final void usbLed(boolean b);

	public static native final void buzzer(boolean on);

	public static void main() {
		Blueboard.init();

		Blueboard.buzzer(true);
		Blueboard.delay(300);
		Blueboard.buzzer(false);

		while (true) {
			for (int i = 0; i <= 99; i++) {
				Blueboard.usbLed(true);
				Blueboard.delay(50);
				Blueboard.usbLed(false);
				Blueboard.delay(50);

				String s = "Davs " + "dux: " + i;
				Blueboard.outString(s);
				Blueboard.rs232OutString(s);
				Blueboard.delay(950);
				if (i % 10 == 0) {
					for (int j = 0; j < 1000; j++) {
						new Object();
					}
				}
			}
		}
	}
}
