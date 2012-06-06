package thinj.gameduino;

public class GDColor {
	// The size in bytes of a color:
	public static final int COLOR_SIZE = 2;

	public static final GDColor WHITE = new GDColor(31, 31, 31);
	public static final GDColor RED = new GDColor(31, 0, 0);
	public static final GDColor GREEN = new GDColor(0, 31, 0);
	public static final GDColor BLUE = new GDColor(0, 0, 31);
	public static final GDColor BLACK = new GDColor(0, 0, 0);

	// The 16-bit attribute holding the Gameduino encoded color:
	private int aColor;

	/**
	 * Constructs a non-transparent color
	 * 
	 * @param transparent true, if the color is transparent; false if solid
	 * @param r The red value. is binary AND'ed with 0x1f before use
	 * @param g The green value. is binary AND'ed with 0x1f before use
	 * @param b The blue value. is binary AND'ed with 0x1f before use
	 */
	public GDColor(int r, int g, int b) {
		aColor = ((r & 0x1f) << 10) + ((g & 0x1f) << 5) + (b & 0x1f);
	}

	/**
	 * Constructs a black or transparent color
	 * 
	 * @param transparent true, if the color is transparent; false if solid and black
	 */
	public GDColor(boolean transparent) {
		aColor = transparent ? 0x8000 : 0;
	}

	/**
	 * This method sets the red component of the colour
	 * 
	 * @param value The colour component value. is binary AND'ed with 0x1f before use
	 */
	public void setRed(int value) {
		aColor = aColor & 0x83ff;
		aColor = aColor | ((value & 0x1f) << 10);
	}

	/**
	 * This method gets the red component of the colour
	 * 
	 * @return The colour value in the interval 0..31
	 */
	public int getRed() {
		return (aColor >> 10) & 0x1f;
	}

	/**
	 * This method sets the green component of the color
	 * 
	 * @param value The colour component value. is binary AND'ed with 0x1f before use
	 */
	public void setGreen(int value) {
		aColor = aColor & 0xfc1f;
		aColor = aColor | ((value & 0x1f) << 5);
	}

	/**
	 * This method gets the green component of the color
	 * 
	 * @return The colour value in the interval 0..31
	 */
	public int getGreen() {
		return (aColor >> 5) & 0x1f;
	}

	/**
	 * This method sets the blue component of the color
	 * 
	 * @param value The colour component value. is binary AND'ed with 0x1f before use
	 */
	public void setBlue(int value) {
		aColor = aColor & 0xffc0;
		aColor = aColor | (value & 0x1f);
	}

	/**
	 * This method gets the blue component of the color
	 * 
	 * @return The colour value in the interval 0..31
	 */
	public int getBlue() {
		return aColor & 0x1f;
	}

	/**
	 * This method returns true if the color is transparent; false otherwise
	 * 
	 * @return true if the color is transparent; false otherwise
	 */
	public boolean isTranparent() {
		return (aColor & 0x8000) != 0;
	}

	/**
	 * This method sets transparency. The color component values are not changed.
	 * 
	 * @param transparent true, if the color shall be transparent; false if non-transparent.
	 */
	public void setTransparent(boolean transparent) {
		aColor = 0x7fff | (transparent ? 0x8000 : 0);
	}

	/**
	 * This method returns the Gameduino encoded color value
	 * 
	 * @return The Gameduino encoded color value
	 */
	public int getColor() {
		return aColor;
	}

	/**
	 * This method sets the Gameduino encoded color value
	 * 
	 * @param The Gameduino encoded color value. Is binary AND'ed with 0xffff before use.
	 */
	public void setColor(int color) {
		aColor = color & 0xffff;
	}

	/**
	 * This method transfers the color to the Gameduino at the specified address
	 * 
	 * @param address The address in Gameduino memory space where this color shall be stored
	 */
	public void write(int address) {		
		byte[] ba = new byte[2];
		ba[0] = (byte) (aColor & 0xff);
		ba[1] = (byte) ((aColor >> 8) & 0xff);
		Gameduino.writeBytes(address, ba);
	}
	
	/**
	 * This method transfers the color to the Gameduino at the specified address
	 * 
	 * @param address The address in Gameduino memory space where this color shall be stored
	 */
	public void read(int address) {		
		byte[] ba = new byte[2];
		ba[0] = (byte) (aColor & 0xff);
		ba[1] = (byte) ((aColor >> 8) & 0xff);
		Gameduino.readBytes(address, ba);
		aColor = (ba[1] & 0xff) << 8;
		aColor += ba[0] & 0xff;
	}
}
