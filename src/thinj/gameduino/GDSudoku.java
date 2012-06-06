package thinj.gameduino;

public class GDSudoku {
	private static int count = 0;
	private static int[][] initialBoard;

	private static void magnify(int srcCh, int dstCh, GDColor color) {		
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
		

		// Paint frame lines:
		for (int i = 0; i < 8; i++) {
			dst[1].setPixel(7, i, 2);
			dst[3].setPixel(7, i, 2);
			dst[2].setPixel(i, 7, 2);
			dst[3].setPixel(i, 7, 2);
		}

		for (int i = 0; i < dst.length; i++) {
			dst[i].getPalette()[0] = GDColor.WHITE;
			dst[i].getPalette()[1] = GDColor.BLUE;
			dst[i].getPalette()[2] = GDColor.GREEN;
			dst[i].getPalette()[3] = color;

			dst[i].write(dstCh + i);
		}
	}

	private static void createDoubleChar(int i) {
		magnify(i == 0 ? ' ' : (i + '0'), 100 + i * 4, GDColor.BLUE);
		magnify(i == 0 ? ' ' : (i + '0'), 200 + i * 4, GDColor.RED);
	}

	private static void display(int value, int x, int y) {
		int xOffset = (400 - 9 * 16) / 16;
		int yOffset = (300 - 9 * 16) / 16;
		int charOffset;
		if (initialBoard[x][y] != 0) {
			charOffset = 200;
		} else {
			charOffset = 100;
		}
		Gameduino.writeByte(xOffset + 0 + x * 2 + 0x40 * (yOffset + 0 + y * 2), charOffset + 4
				* value);
		Gameduino.writeByte(xOffset + 1 + x * 2 + 0x40 * (yOffset + 0 + y * 2), charOffset + 4
				* value + 1);
		Gameduino.writeByte(xOffset + 0 + x * 2 + 0x40 * (yOffset + 1 + y * 2), charOffset + 4
				* value + 2);
		Gameduino.writeByte(xOffset + 1 + x * 2 + 0x40 * (yOffset + 1 + y * 2), charOffset + 4
				* value + 3);
	}

	private static void display(int[][] cells) {
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				display(cells[x][y], x, y);
			}

		}
	}

	public static boolean solve(int[][] cells, int ij) {
		if ((count & 0xff) == 0) {
			display(cells);
			System.out.println("Count: " + count);
			if (count % 8000 == 0) {
				System.out.println();
			}
		}
		count++;
		if (ij >= 81) {
			return true; // solved
		}
		int i = ij / 9;
		int j = ij % 9;
		// Solve first unsolved cell:
		if (cells[i][j] != 0) {
			return solve(cells, ij + 1);
		}
		// Unsolved; try each possible value:
		boolean[] usedValues = getUsedValues(cells, i, j);
		// Start at 1; 0 is not a legal value:
		for (cells[i][j] = 1; cells[i][j] < usedValues.length; cells[i][j]++) {
			if (!usedValues[cells[i][j]] && solve(cells, ij + 1)) {
				display(cells);
				return true;
			}
		}
		cells[i][j] = 0; // 'back tracking'
		return false;
	}

	/**
	 * Find all values not used in row, column or box identified by i, j
	 * 
	 * @return All values not used in row, column or box identified by i, j
	 */
	private static boolean[] getUsedValues(int[][] cells, int i, int j) {
		boolean used[] = { false, false, false, false, false, false, false, false, false, false };
		// Row and column:
		for (int n = 0; n < 9; n++) {
			used[cells[n][j]] = true;
			used[cells[i][n]] = true;
		}
		// Box
		int iBase = i - i % 3;
		int jBase = j - j % 3;
		for (int di = 0; di < 3; di++) {
			for (int dj = 0; dj < 3; dj++) {
				used[cells[iBase + di][jBase + dj]] = true;
			}
		}

		return used;
	}

	public static void main() {
		System.out.println(Gameduino.isAvailable() ? "Gameduino is mounted" : "No Gameduino");

		if (Gameduino.isAvailable()) {
			// Set all sprite colors to transparent:
			int lo = 400 % 256;
			int hi = 400 / 256;
			for (int i = 0; i < 256; i++) {
				Gameduino.writeByte(0x3000 + i * 4, lo);
				Gameduino.writeByte(0x3000 + i * 4 + 1, hi);
				Gameduino.writeByte(0x3000 + i * 4 + 2, lo);
				Gameduino.writeByte(0x3000 + i * 4 + 3, hi);
			}

			// Fill character area with space:
			for (int i = 0; i < 0x1000; i++) {
				Gameduino.writeByte(i, ' ');
			}
			for (int i = 0; i < 10; i++) {
				createDoubleChar(i);
			}

			int[][] board1 = new int[][] { { 0, 5, 0, 0, 6, 0, 0, 0, 1 },
					{ 0, 0, 4, 8, 0, 0, 0, 7, 0 }, { 8, 0, 0, 0, 0, 0, 0, 5, 2 },
					{ 2, 0, 0, 0, 5, 7, 0, 3, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
					{ 0, 3, 0, 6, 9, 0, 0, 0, 5 }, { 7, 9, 0, 0, 0, 0, 0, 0, 8 },
					{ 0, 1, 0, 0, 0, 6, 5, 0, 0 }, { 5, 0, 0, 0, 3, 0, 0, 6, 0 } };
			initialBoard = new int[][] { { 0, 5, 0, 0, 6, 0, 0, 0, 1 },
					{ 0, 0, 4, 8, 0, 0, 0, 7, 0 }, { 8, 0, 0, 0, 0, 0, 0, 5, 2 },
					{ 2, 0, 0, 0, 5, 7, 0, 3, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
					{ 0, 3, 0, 6, 9, 0, 0, 0, 5 }, { 7, 9, 0, 0, 0, 0, 0, 0, 8 },
					{ 0, 1, 0, 0, 0, 6, 5, 0, 0 }, { 5, 0, 0, 0, 3, 0, 0, 6, 0 } };
			solve(board1, 0);
		}
	}
}
