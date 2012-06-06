package thinj.regression.arrays;

import thinj.regression.Regression;

public class Sudoku {
	private static int count = 0;

	public static boolean solve(int[][] cells, int ij) {
		count++;
		if (count % 100 == 0) {
			// System.out.println("sudoku: " + count);
			System.out.print(".");
			if (count % 8000 == 0) {
				System.out.println();
			}
		}
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
		int[][] board1 = new int[][] { { 0, 5, 0, 0, 6, 0, 0, 0, 1 },
				{ 0, 0, 4, 8, 0, 0, 0, 7, 0 }, { 8, 0, 0, 0, 0, 0, 0, 5, 2 },
				{ 2, 0, 0, 0, 5, 7, 0, 3, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 3, 0, 6, 9, 0, 0, 0, 5 }, { 7, 9, 0, 0, 0, 0, 0, 0, 8 },
				{ 0, 1, 0, 0, 0, 6, 5, 0, 0 }, { 5, 0, 0, 0, 3, 0, 0, 6, 0 } };

		int[][] res = new int[][] { { 9, 5, 3, 7, 6, 2, 8, 4, 1 }, { 6, 2, 4, 8, 1, 5, 9, 7, 3 },
				{ 8, 7, 1, 3, 4, 9, 6, 5, 2 }, { 2, 8, 9, 4, 5, 7, 1, 3, 6 },
				{ 1, 6, 5, 2, 8, 3, 4, 9, 7 }, { 4, 3, 7, 6, 9, 1, 2, 8, 5 },
				{ 7, 9, 6, 5, 2, 4, 3, 1, 8 }, { 3, 1, 8, 9, 7, 6, 5, 2, 4 },
				{ 5, 4, 2, 1, 3, 8, 7, 6, 9 } };
		solve(board1, 0);
		System.out.println();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				Regression.verify(board1[i][j] == res[i][j]);
			}
		}		
	}
}
