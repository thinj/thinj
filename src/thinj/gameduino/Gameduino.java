package thinj.gameduino;

public class Gameduino {
	public static final int IDENT = 0x2800;
	public static final int SCROLL_X = 0x2804;
	public static final int SCROLL_Y = 0x2806;

	public static native void writeShort(int address, int value);

	public static native void writeByte(int address, int value);

	public static native void fill(int address, int value, int length);

	public static native int readByte(int address);

	public static native void readBytes(int address, byte[] bytes);

	public static native void writeBytes(int address, byte[] bytes);

	/**
	 * This method returns true if the Gameduino board is mounted
	 * 
	 * @return true if the Gameduino board is mounted; false otherwise.
	 */
	public static boolean isAvailable() {
		return readByte(IDENT) == 0x6D;
	}

	private static void magnify(int srcCh, int dstCh) {
		GDCharacter src = new GDCharacter();
		src.read(srcCh);
		GDCharacter[] dst = new GDCharacter[4];
		for (int i = 0; i < dst.length; i++) {
			dst[i] = new GDCharacter();
		}
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				int pixel = src.getPixel(x / 2, y / 2);
				dst[0].setPixel(x, y, pixel);

				pixel = src.getPixel(4 + x / 2, y / 2);
				dst[1].setPixel(x, y, pixel);

				pixel = src.getPixel(x / 2, 4 + y / 2);
				dst[2].setPixel(x, y, pixel);

				pixel = src.getPixel(4 + x / 2, 4 + y / 2);
				dst[3].setPixel(x, y, pixel);
			}
		}

		for (int i = 0; i < dst.length; i++) {
			dst[i].getPalette()[0] = GDColor.WHITE;
			dst[i].getPalette()[1] = GDColor.BLUE;
			dst[i].getPalette()[2] = GDColor.GREEN;
			dst[i].getPalette()[3] = GDColor.RED;
			// dst[i].setPalette(src.getPalette());
			dst[i].write(dstCh + i);
		}
	}

	public static void main() {
		System.out.println(isAvailable() ? "Gameduino is mounted" : "No Gameduino");

		if (isAvailable()) {
			magnify('3', 'A');
			writeByte(0x40, 'A');
			writeByte(0x41, 'A' + 1);
			writeByte(0x80, 'A' + 2);
			writeByte(0x81, 'A' + 3);
			GDCharacter a = new GDCharacter();
			a.read('T');
			a.getPalette()[0] = GDColor.WHITE;
			a.getPalette()[1] = GDColor.BLUE;
			a.getPalette()[2] = GDColor.GREEN;
			a.getPalette()[3] = GDColor.RED;
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < 8; x++) {
					int pixel = a.getPixel(x, y);
					a.setPixel(x, y, pixel);
				}
			}
			a.write('T');
			// GDCharacter a = new GDCharacter();
			// a.read('T');
			// a.getPalette()[0] = GDColor.WHITE;
			// a.getPalette()[1] = GDColor.BLUE;
			// a.getPalette()[2] = GDColor.GREEN;
			// a.getPalette()[3] = GDColor.RED;
			//
			// for (int y = 0; y < 8; y++) {
			// for (int x = 0; x < 8; x++) {
			// a.setPixel(x, y, 0);
			// }
			// }
			//
			// for (int x = 0; x < 8; x += 2) {
			// a.setPixel(x, x, 1);
			// a.setPixel(x + 1, x + 1, 3);
			// }
			//
			// a.write('T');
			//
			// for (int i = 0; i < 0x1000; i += 2) {
			// writeByte(i, 'A');
			// writeByte(i + 1, 'T');
			// }

			// int i = 0;
			// while (true) {
			// System.out
			// .println("scrolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll");
			// writeShort(SCROLL_X, i);
			// writeShort(SCROLL_Y, i++);
			// }
		}
	}
}
