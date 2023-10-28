package com.jamesratzlaff.util.bit;

import java.util.ArrayList;
import java.util.List;

public class BitStringStream {

	public static final char[] FOUR_BIT_RESOLUTION={' ','▀','▄','█'};
	private static long getBit(long[] bits, int x, int y, int rowSize) {
		int offset = y * rowSize + (x >>> BitUnit.LONG.multOrDivShift());
		return ((bits[offset] >>> (x & BitUnit.LONG.limitMask())) & 1l);
	}
	
	private static byte getBitsAsByte(long[] bits, int x, int y, int rowSize, int amtToRead) {
		int xOffset = (x >>> BitUnit.LONG.multOrDivShift());
		int xShift = (x & BitUnit.LONG.limitMask());
		byte result=0;
		for(int i=0;i<amtToRead;i++) {
			int offset = ((y+i) * rowSize)+(xOffset);
			if(offset>=bits.length) {
				break;
			}
			result|= (((bits[offset] >>> xShift) & 1l)<<i);
		}
		return result;
	}
	
	private static byte[] compressBitsTo1x2(long[] values, int width) {
		int rowSize = (width + BitUnit.LONG.limitMask()) >>>BitUnit.LONG.multOrDivShift();
		int rows = values.length/rowSize;
		int oddRows = rows&1;
		byte[] result=new byte[(width*(rows>>1))+(oddRows*width)];
		for(int y=0;y<(rows+oddRows);y+=2) {
			for(int x=0;x<width;x++) {
				byte val=getBitsAsByte(values, x, y, rowSize, 2);
				int offset = ((y*width)>>>1)+x;
				result[offset]=val;
			}
		}
		return result;
	}
	public static String toString(long[] values, int width) {
		List<String> strings = toStrings(values,width);
		return String.join("\n", strings);
	}
	public static List<String> toStrings(long[] values, int width){
		byte[] bytes = compressBitsTo1x2(values, width);
		List<String> strings = toStrings(bytes,width);
		return strings;
	}
	public static List<String> toStrings(byte[] bytes, int width){
		int rowSize = (width + BitUnit.LONG.limitMask()) >>>BitUnit.LONG.multOrDivShift();
		int rows = bytes.length/width;
		List<String> strings = new ArrayList<String>(rows);
		int oddRows = rows&1;
		int iterRows = rows+oddRows;
		for(int row=0;row<iterRows;row++) {
			StringBuilder sb = new StringBuilder(width);
			for(int x=0;x<width;x++) {
				int offset = (row*width)+x;
				byte b = bytes[offset];
				sb.append(FOUR_BIT_RESOLUTION[b]);
			}
			strings.add(sb.toString());
		}
		return strings;
	}
	
	
}
