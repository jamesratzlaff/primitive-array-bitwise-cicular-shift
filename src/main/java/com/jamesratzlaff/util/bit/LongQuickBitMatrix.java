package com.jamesratzlaff.util.bit;

import java.util.Arrays;

public class LongQuickBitMatrix {
	private static final BitUnit unit = BitUnit.LONG;

	private int width;
	private int height;
	private int rowSize;
	private long[] bits;

	/**
	 * Creates an empty square {@code BitMatrix}.
	 *
	 * @param dimension height and width
	 */
	public LongQuickBitMatrix(int dimension) {
		this(dimension, dimension);
	}

	/**
	 * Creates an empty {@code BitMatrix}.
	 *
	 * @param width  bit matrix width
	 * @param height bit matrix height
	 */
	public LongQuickBitMatrix(int width, int height) {
		if (width < 1 || height < 1) {
			throw new IllegalArgumentException("Both dimensions must be greater than 0");
		}
		this.width = width;
		this.height = height;
		this.rowSize = (width + unit.limitMask()) >>>unit.bits();
		bits = new long[rowSize * height];
	}

	private LongQuickBitMatrix(int width, int height, int rowSize, long[] bits) {
	    this.width = width;
	    this.height = height;
	    this.rowSize = rowSize;
	    this.bits = bits;
	  }

	/**
	 * Interprets a 2D array of booleans as a {@code BitMatrix}, where "true" means
	 * an "on" bit.
	 *
	 * @param image bits of the image, as a row-major 2D array. Elements are arrays
	 *              representing rows
	 * @return {@code BitMatrix} representation of image
	 */
	public static LongQuickBitMatrix parse(boolean[][] image) {
		int height = image.length;
		int width = image[0].length;
		LongQuickBitMatrix bits = new LongQuickBitMatrix(width, height);
		for (int i = 0; i < height; i++) {
			boolean[] imageI = image[i];
			for (int j = 0; j < width; j++) {
				if (imageI[j]) {
					bits.set(j, i);
				}
			}
		}
		return bits;
	}

	public static LongQuickBitMatrix parse(String stringRepresentation, String setString, String unsetString) {
		if (stringRepresentation == null) {
			throw new IllegalArgumentException();
		}

		boolean[] bits = new boolean[stringRepresentation.length()];
		int bitsPos = 0;
		int rowStartPos = 0;
		int rowLength = -1;
		int nRows = 0;
		int pos = 0;
		while (pos < stringRepresentation.length()) {
			if (stringRepresentation.charAt(pos) == '\n' || stringRepresentation.charAt(pos) == '\r') {
				if (bitsPos > rowStartPos) {
					if (rowLength == -1) {
						rowLength = bitsPos - rowStartPos;
					} else if (bitsPos - rowStartPos != rowLength) {
						throw new IllegalArgumentException("row lengths do not match");
					}
					rowStartPos = bitsPos;
					nRows++;
				}
				pos++;
			} else if (stringRepresentation.startsWith(setString, pos)) {
				pos += setString.length();
				bits[bitsPos] = true;
				bitsPos++;
			} else if (stringRepresentation.startsWith(unsetString, pos)) {
				pos += unsetString.length();
				bits[bitsPos] = false;
				bitsPos++;
			} else {
				throw new IllegalArgumentException(
						"illegal character encountered: " + stringRepresentation.substring(pos));
			}
		}

		// no EOL at end?
		if (bitsPos > rowStartPos) {
			if (rowLength == -1) {
				rowLength = bitsPos - rowStartPos;
			} else if (bitsPos - rowStartPos != rowLength) {
				throw new IllegalArgumentException("row lengths do not match");
			}
			nRows++;
		}

		LongQuickBitMatrix matrix = new LongQuickBitMatrix(rowLength, nRows);
		for (int i = 0; i < bitsPos; i++) {
			if (bits[i]) {
				matrix.set(i % rowLength, i / rowLength);
			}
		}
		return matrix;
	}

	/**
	 * <p>
	 * Gets the requested bit, where true means black.
	 * </p>
	 *
	 * @param x The horizontal component (i.e. which column)
	 * @param y The vertical component (i.e. which row)
	 * @return value of given bit in matrix
	 */
	public boolean get(int x, int y) {
		int offset = y * rowSize + (x >>> unit.multOrDivShift());
		return ((bits[offset] >>> (x & unit.limitMask())) & 1l) != 0;
	}

	/**
	 * <p>
	 * Sets the given bit to true.
	 * </p>
	 *
	 * @param x The horizontal component (i.e. which column)
	 * @param y The vertical component (i.e. which row)
	 */
	public void set(int x, int y) {
		int offset = y * rowSize + (x >>> unit.multOrDivShift());
		bits[offset] |= 1l << (x & unit.limitMask());
	}

	public void unset(int x, int y) {
		int offset = y * rowSize + (x >>> unit.multOrDivShift());
		bits[offset] &= ~(1l << (x & unit.limitMask()));
	}

	/**
	 * <p>
	 * Flips the given bit.
	 * </p>
	 *
	 * @param x The horizontal component (i.e. which column)
	 * @param y The vertical component (i.e. which row)
	 */
	public void flip(int x, int y) {
		int offset = y * rowSize + (x >>> unit.multOrDivShift());
		bits[offset] ^= 1l << (x & unit.limitMask());
	}

	/**
	 * <p>
	 * Flips every bit in the matrix.
	 * </p>
	 */
	public void flip() {
		int max = bits.length;
		for (int i = 0; i < max; i++) {
			bits[i] = ~bits[i];
		}
	}

	/**
	 * Exclusive-or (XOR): Flip the bit in this {@code BitMatrix} if the
	 * corresponding mask bit is set.
	 *
	 * @param mask XOR mask
	 */
	public void xor(LongQuickBitMatrix mask) {
		if (width != mask.width || height != mask.height || rowSize != mask.rowSize) {
			throw new IllegalArgumentException("input matrix dimensions do not match");
		}
		LongQuickBitArray rowArray = new LongQuickBitArray(width);
		for (int y = 0; y < height; y++) {
			int offset = y * rowSize;
			long[] row = mask.getRow(y, rowArray).getBitArray();
			for (int x = 0; x < rowSize; x++) {
				bits[offset + x] ^= row[x];
			}
		}
	}

	/**
	 * Clears all bits (sets to false).
	 */
	public void clear() {
		int max = bits.length;
		for (int i = 0; i < max; i++) {
			bits[i] = 0;
		}
	}

	/**
	 * <p>
	 * Sets a square region of the bit matrix to true.
	 * </p>
	 *
	 * @param left   The horizontal position to begin at (inclusive)
	 * @param top    The vertical position to begin at (inclusive)
	 * @param width  The width of the region
	 * @param height The height of the region
	 */
	public void setRegion(int left, int top, int width, int height) {
		if (top < 0 || left < 0) {
			throw new IllegalArgumentException("Left and top must be nonnegative");
		}
		if (height < 1 || width < 1) {
			throw new IllegalArgumentException("Height and width must be at least 1");
		}
		int right = left + width;
		int bottom = top + height;
		if (bottom > this.height || right > this.width) {
			throw new IllegalArgumentException("The region must fit inside the matrix");
		}
		for (int y = top; y < bottom; y++) {
			int offset = y * rowSize;
			for (int x = left; x < right; x++) {
				bits[offset + (x >>> unit.multOrDivShift())] |= 1l << (x & unit.limitMask());
			}
		}
	}

	/**
	 * A fast method to retrieve one row of data from the matrix as a BitArray.
	 *
	 * @param y   The row to retrieve
	 * @param row An optional caller-allocated BitArray, will be allocated if null
	 *            or too small
	 * @return The resulting BitArray - this reference should always be used even
	 *         when passing your own row
	 */
	public LongQuickBitArray getRow(int y, LongQuickBitArray row) {
		if (row == null || row.getSize() < width) {
			row = new LongQuickBitArray(width);
		} else {
			row.clear();
		}
		int offset = y * rowSize;
		for (int x = 0; x < rowSize; x++) {
			row.setBulk(x << unit.multOrDivShift(), bits[offset + x]);
		}
		return row;
	}

	/**
	 * @param y   row to set
	 * @param row {@link LongQuickBitArray} to copy from
	 */
	public void setRow(int y, LongQuickBitArray row) {
		System.arraycopy(row.getBitArray(), 0, bits, y * rowSize, rowSize);
	}

	/**
	 * Modifies this {@code BitMatrix} to represent the same but rotated the given
	 * degrees (0, 90, 180, 270)
	 *
	 * @param degrees number of degrees to rotate through counter-clockwise (0, 90,
	 *                180, 270)
	 */
	public void rotate(int degrees) {
		switch (degrees % 360) {
		case 0:
			return;
		case 90:
			rotate90();
			return;
		case 180:
			rotate180();
			return;
		case 270:
			rotate90();
			rotate180();
			return;
		}
		throw new IllegalArgumentException("degrees must be a multiple of 0, 90, 180, or 270");
	}

	/**
	 * Modifies this {@code BitMatrix} to represent the same but rotated 180 degrees
	 */
	public void rotate180() {
		LongQuickBitArray topRow = new LongQuickBitArray(width);
		LongQuickBitArray bottomRow = new LongQuickBitArray(width);
		int maxHeight = (height + 1) / 2;
		for (int i = 0; i < maxHeight; i++) {
			topRow = getRow(i, topRow);
			int bottomRowIndex = height - 1 - i;
			bottomRow = getRow(bottomRowIndex, bottomRow);
			topRow.reverse();
			bottomRow.reverse();
			setRow(i, bottomRow);
			setRow(bottomRowIndex, topRow);
		}
	}

	/**
	 * Modifies this {@code BitMatrix} to represent the same but rotated 90 degrees
	 * counterclockwise
	 */
	public void rotate90() {
		int newWidth = height;
		int newHeight = width;
		int newRowSize = (newWidth + unit.limitMask())>>>unit.multOrDivShift();
		long[] newBits = new long[newRowSize * newHeight];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = y * rowSize + (x >>> unit.multOrDivShift());
				if (((bits[offset] >>> (x & 0x1f)) & 1) != 0) {
					int newOffset = (newHeight - 1 - x) * newRowSize + (y >>> unit.multOrDivShift());
					newBits[newOffset] |= 1l << (y & unit.limitMask());
				}
			}
		}
		width = newWidth;
		height = newHeight;
		rowSize = newRowSize;
		bits = newBits;
	}

	/**
	 * This is useful in detecting the enclosing rectangle of a 'pure' barcode.
	 *
	 * @return {@code left,top,width,height} enclosing rectangle of all 1 bits, or
	 *         null if it is all white
	 */
	public int[] getEnclosingRectangle() {
		int left = width;
		int top = height;
		int right = -1;
		int bottom = -1;

		for (int y = 0; y < height; y++) {
			for (int x32 = 0; x32 < rowSize; x32++) {
				long theBits = bits[y * rowSize + x32];
				if (theBits != 0) {
					if (y < top) {
						top = y;
					}
					if (y > bottom) {
						bottom = y;
					}
					if ((x32 <<unit.multOrDivShift()) < left) {
						int bit = 0;
						while ((theBits << (unit.limitMask() - bit)) == 0) {
							bit++;
						}
						if (((x32 << unit.multOrDivShift()) + bit) < left) {
							left = (x32 << unit.multOrDivShift()) + bit;
						}
					}
					if ((x32 <<unit.multOrDivShift()) + unit.limitMask() > right) {
						int bit = unit.limitMask();
						while ((theBits >>> bit) == 0) {
							bit--;
						}
						if (((x32 << unit.multOrDivShift()) + bit) > right) {
							right = (x32 << unit.multOrDivShift()) + bit;
						}
					}
				}
			}
		}

		if (right < left || bottom < top) {
			return null;
		}

		return new int[] { left, top, right - left + 1, bottom - top + 1 };
	}

	/**
	 * This is useful in detecting a corner of a 'pure' barcode.
	 *
	 * @return {@code x,y} coordinate of top-left-most 1 bit, or null if it is all
	 *         white
	 */
	public int[] getTopLeftOnBit() {
		int bitsOffset = 0;
		while (bitsOffset < bits.length && bits[bitsOffset] == 0) {
			bitsOffset++;
		}
		if (bitsOffset == bits.length) {
			return null;
		}
		int y = bitsOffset / rowSize;
		int x = (bitsOffset % rowSize) << unit.multOrDivShift();

		long theBits = bits[bitsOffset];
		int bit = 0;
		while ((theBits << (unit.limitMask() - bit)) == 0) {
			bit++;
		}
		x += bit;
		return new int[] { x, y };
	}

	public int[] getBottomRightOnBit() {
		int bitsOffset = bits.length - 1;
		while (bitsOffset >= 0 && bits[bitsOffset] == 0) {
			bitsOffset--;
		}
		if (bitsOffset < 0) {
			return null;
		}

		int y = bitsOffset / rowSize;
		int x = (bitsOffset % rowSize) << unit.multOrDivShift();

		long theBits = bits[bitsOffset];
		int bit = unit.limitMask();
		while ((theBits >>> bit) == 0) {
			bit--;
		}
		x += bit;

		return new int[] { x, y };
	}

	/**
	 * @return The width of the matrix
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return The height of the matrix
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return The row size of the matrix
	 */
	public int getRowSize() {
		return rowSize;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LongQuickBitMatrix)) {
			return false;
		}
		LongQuickBitMatrix other = (LongQuickBitMatrix) o;
		return width == other.width && height == other.height && rowSize == other.rowSize
				&& Arrays.equals(bits, other.bits);
	}

	@Override
	public int hashCode() {
		int hash = width;
		hash = 31 * hash + width;
		hash = 31 * hash + height;
		hash = 31 * hash + rowSize;
		hash = 31 * hash + Arrays.hashCode(bits);
		return hash;
	}

	/**
	 * @return string representation using "X" for set and " " for unset bits
	 */
	@Override
	public String toString() {
		return toString("X ", "  ");
	}

	/**
	 * @param setString   representation of a set bit
	 * @param unsetString representation of an unset bit
	 * @return string representation of entire matrix utilizing given strings
	 */
	public String toString(String setString, String unsetString) {
		return buildToString(setString, unsetString, "\n");
	}

	/**
	 * @param setString     representation of a set bit
	 * @param unsetString   representation of an unset bit
	 * @param lineSeparator newline character in string representation
	 * @return string representation of entire matrix utilizing given strings and
	 *         line separator
	 * @deprecated call {@link #toString(String,String)} only, which uses \n line
	 *             separator always
	 */
	@Deprecated
	public String toString(String setString, String unsetString, String lineSeparator) {
		return buildToString(setString, unsetString, lineSeparator);
	}

	private String buildToString(String setString, String unsetString, String lineSeparator) {
		StringBuilder result = new StringBuilder(height * (width + 1));
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				result.append(get(x, y) ? setString : unsetString);
			}
			result.append(lineSeparator);
		}
		return result.toString();
	}

	@Override
	public LongQuickBitMatrix clone() {
		return new LongQuickBitMatrix(width, height, rowSize, bits.clone());
	}
	
	public void rotX(int amount) {
		LongArrayShift.rotate(bits, amount);
	}

}
