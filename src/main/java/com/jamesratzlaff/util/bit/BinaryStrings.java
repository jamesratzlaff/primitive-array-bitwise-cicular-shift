package com.jamesratzlaff.util.bit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

	public static String toBinaryString(List<Number> nums) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(String.join(",",toBinaryStringList(nums)));
		sb.append(']');
		return sb.toString();
	}
	
	private static List<String> toBinaryStringList(List<Number> bits) {
		return bits.stream().map(BinaryStrings::toBinaryString).collect(Collectors.toList());
	}

	private static final int getBits(Number n) {
		Class<? extends Number> numberClass = n.getClass();
		String clazzName = numberClass.getSimpleName();
		int bits = switch (clazzName) {
		case "Byte" -> {
			yield Byte.SIZE;
		}
		case "Short" -> {
			yield Short.SIZE;
		}
		case "Integer" -> {
			yield Integer.SIZE;
		}
		case "Long" -> {
			yield Long.SIZE;
		}
		case "Float" -> {
			yield Float.SIZE;
		}
		case "Double" -> {
			yield Double.SIZE;
		}
		default -> {
			yield 0;
		}
		};
		return bits;
	}
	private static final Function<Number,String> getToBinaryString(Number n) {
		Class<? extends Number> numberClass = n.getClass();
		String clazzName = numberClass.getSimpleName();
		Function<Number,String> func = switch (clazzName) {
		case "Byte" -> {
			yield (num)->Integer.toBinaryString(0xFF&num.byteValue());
		}
		case "Short" -> {
			yield (num)->Integer.toBinaryString(0xFFFF&num.shortValue());
		}
		case "Integer" -> {
			yield (num)->Integer.toBinaryString(num.intValue());
		}
		case "Long" -> {
			yield (num)->Long.toBinaryString(num.longValue());
		}
		case "Float" -> {
			yield (num)->Integer.toBinaryString(Float.floatToIntBits(num.floatValue()));
		}
		case "Double" -> {
			yield  (num)->Long.toBinaryString(Double.doubleToRawLongBits(num.doubleValue()));
		}
		default -> {
			yield (num)->Long.toBinaryString(num.longValue());
		}
		};
		return func;
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
	public static String toBinaryString(Number value) {
		int minBits = getBits(value);
		var toBinStrFunc = getToBinaryString(value);
		String binString = toBinStrFunc.apply(value);
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
		if (size < 0) {
			size = BitUnit.LONG.bits() * bits.length;
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

	public static String toBinaryString(long value, long... values) {
		long[] all = new long[values.length + 1];
		all[0] = value;
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
