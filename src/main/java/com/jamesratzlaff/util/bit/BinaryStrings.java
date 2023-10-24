package com.jamesratzlaff.util.bit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BinaryStrings {
	public static String toBinaryString(int[] bits) {
		return toBinaryString(bits, ",");
	}

	public static String toBinaryString(int[] bits, int size) {
		return toBinaryString(bits, size, ",");
	}

	public static String toBinaryString(int[] bits, String separator) {
		int size = BitUnit.INT.bits() * bits.length;
		return toBinaryString(bits, size, separator);
	}

	public static String toBinaryString(int[] bits, int size, String separator) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		List<String> strs = toStringList(bits, size);
		sb.append(String.join(separator, strs));
		sb.append(']');
		return sb.toString();
	}

	private static List<String> toStringList(int[] bits, int size) {
		List<String> strs = new ArrayList<String>(bits.length);
		for (int i = 0; i < bits.length; i++) {
			int offset = BitUnit.INT.bits() * i;
			int len = (BitUnit.INT.bits() + offset) < size ? BitUnit.INT.bits() : (size - offset);
			strs.add(toBinaryString(bits[i], len));

		}
		return strs;

	}

	public static String toBinaryString(int value) {
		return toBinaryString(value, BitUnit.INT.bits());
	}

	public static String toBinaryString(int value, int minBits) {
		String binString = Integer.toBinaryString(value);
		int zerosNeeded = minBits - binString.length();
		if (zerosNeeded > 0) {
			char[] zeros = new char[zerosNeeded];
			Arrays.fill(zeros, '0');
			String zerosString = new String(zeros);
			binString = zerosString + binString;
		}
		return binString;
	}
	
	public static String toBinaryString(long[] bits) {
		return toBinaryString(bits, ",");
	}

	public static String toBinaryString(long[] bits, int size) {
		return toBinaryString(bits, size, ",");
	}

	public static String toBinaryString(long[] bits, String separator) {
		int size = BitUnit.LONG.bits() * bits.length;
		return toBinaryString(bits, size, separator);
	}

	public static String toBinaryString(long[] bits, int size, String separator) {
		if(size<0) {
			size=BitUnit.LONG.bits()*bits.length;
		}
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		List<String> strs = toStringList(bits, size);
		sb.append(String.join(separator, strs));
		sb.append(']');
		return sb.toString();
	}

	private static List<String> toStringList(long[] bits, int size) {
		List<String> strs = new ArrayList<String>(bits.length);
		for (int i = 0; i < bits.length; i++) {
			int offset = BitUnit.LONG.bits() * i;
			int len = (BitUnit.LONG.bits() + offset) < size ? BitUnit.LONG.bits() : (size - offset);
			strs.add(toBinaryString(bits[i], len));

		}
		return strs;

	}
	public static String toBinaryString(long value, long...values) {
		long[] all = new long[values.length+1];
		all[0]=value;
		System.arraycopy(values, 0, all, 1, values.length);
		return toBinaryString(all);
	}
	public static String toBinaryString(long value) {
		return toBinaryString(value, BitUnit.LONG.bits());
	}

	public static String toBinaryString(long value, int minBits) {
		String binString = Long.toBinaryString(value);
		int zerosNeeded = minBits - binString.length();
		if (zerosNeeded > 0) {
			char[] zeros = new char[zerosNeeded];
			Arrays.fill(zeros, '0');
			String zerosString = new String(zeros);
			binString = zerosString + binString;
		}
		return binString;
	}

}
