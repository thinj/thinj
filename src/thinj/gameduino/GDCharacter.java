package thinj.gameduino;

public class GDCharacter {
	// The number of colors in a character palette:
	private static final int CHARACTER_PALETTE_SIZE = 4;

	public static final int CHARACTER_DATA = 0x1000;
	public static final int CHARACTER_PALETTES = 0x2000;

	private GDColor[] aPalette;
	private byte[] aCharacter;

	public GDCharacter() {
		setPalette(new GDColor[] { GDColor.BLACK, GDColor.BLACK, GDColor.BLACK, GDColor.BLACK });
		aCharacter = new byte[16];
	}

	public GDColor[] getPalette() {
		return aPalette;
	}

	public void setPalette(GDColor[] palette) {
		aPalette = palette;
	}

	/**
	 * This method sets the color of a pixel
	 * 
	 * @param x The x-position of the pixel. Is AND'ed with 7 before usage
	 * @param y The y-position of the pixel. Is AND'ed with 7 before usage
	 * @param paletteIndex The index into the palette. Is AND'ed with 3 before usage
	 */
	public void setPixel(int x, int y, int paletteIndex) {
		y = y & 0x7;
		x = x & 0x7;
		paletteIndex = paletteIndex & 0x3;

		int byteIndex = 2 * y + x / 4;
		int bitPos = 6 - (2 * (x % 4)); // 0, 2, 4, 6
		int mask = 3 << bitPos;
		aCharacter[byteIndex] &= ~mask;
		aCharacter[byteIndex] |= paletteIndex << bitPos;
	}

	/**
	 * This method returns  the color of a pixel
	 * 
	 * @param x The x-position of the pixel. Is AND'ed with 7 before usage
	 * @param y The y-position of the pixel. Is AND'ed with 7 before usage
	 * @return The paletteIndex at the given position
	 */
	public int getPixel(int x, int y) {
		y = y & 0x7;
		x = x & 0x7;

		int byteIndex = 2 * y + x / 4;
		int bitPos = 6 - (2 * (x % 4)); // 0, 2, 4, 6
		return (aCharacter[byteIndex] >> bitPos) & 3;
	}

	/**
	 * This method writes the character definition to the Gameduino
	 * 
	 * @param character The ASCII value; e.g. 65 for 'A'
	 */
	public void write(int character) {
		Gameduino.writeBytes(CHARACTER_DATA + 16 * character, aCharacter);
		for (int paletteIndex = 0; paletteIndex < 4; paletteIndex++) {
			aPalette[paletteIndex].write(CHARACTER_PALETTES + GDColor.COLOR_SIZE
					* (CHARACTER_PALETTE_SIZE * character + paletteIndex));
		}
	}

	/**
	 * This method reads the character definition from the Gameduino
	 * 
	 * @param character The ASCII value; e.g. 65 for 'A'
	 */
	public void read(int character) {
		Gameduino.readBytes(CHARACTER_DATA + 16 * character, aCharacter);
		for (int paletteIndex = 0; paletteIndex < 4; paletteIndex++) {
			aPalette[paletteIndex].read(CHARACTER_PALETTES + GDColor.COLOR_SIZE
					* (CHARACTER_PALETTE_SIZE * character + paletteIndex));
		}

	}

}
