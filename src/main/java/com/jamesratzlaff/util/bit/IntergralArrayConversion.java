package com.jamesratzlaff.util.bit;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

public interface IntergralArrayConversion<A> {
	public static class MASKS {
		public static class LONG{
			public static final long[] FOR_BYTES=IntergralArrayConversion.longMasksFor(BitUnit.BYTE);
			public static final long[] FOR_SHORTS=IntergralArrayConversion.longMasksFor(BitUnit.SHORT);
			public static final long[] FOR_INTS=IntergralArrayConversion.longMasksFor(BitUnit.INT);
			public static byte[] toBytes(long val) {
				long[] masks=FOR_BYTES;
				byte[] result = new byte[masks.length];
				for(int i=0;i<result.length;i++) {
					result[i]=(byte)(val&masks[i]);
				}
				return result;
			}
			public static short[] toShorts(long val) {
				long[] masks=FOR_SHORTS;
				short[] result = new short[masks.length];
				for(int i=0;i<result.length;i++) {
					result[i]=(short)(val&masks[i]);
				}
				return result;
			}
			public static int[] toInts(long val) {
				long[] masks=FOR_INTS;
				int[] result = new int[masks.length];
				for(int i=0;i<result.length;i++) {
					result[i]=(int)(val&masks[i]);
				}
				return result;
			}
		}
		public static class INT {
			public static final int[] FOR_BYTES=IntergralArrayConversion.intMasksFor(BitUnit.BYTE);
			public static final int[] FOR_SHORTS=IntergralArrayConversion.intMasksFor(BitUnit.SHORT);
			public static byte[] toBytes(int val) {
				int[] masks=FOR_BYTES;
				byte[] result = new byte[masks.length];
				for(int i=0;i<result.length;i++) {
					result[i]=(byte)(val&masks[i]);
				}
				return result;
			}
			public static short[] toShorts(int val) {
				int[] masks=FOR_SHORTS;
				short[] result = new short[masks.length];
				for(int i=0;i<result.length;i++) {
					result[i]=(short)(val&masks[i]);
				}
				return result;
			}
		}
		public static class SHORT {
			public static final short[] FOR_BYTES=IntergralArrayConversion.shortMasksFor(BitUnit.BYTE);
			public static byte[] toBytes(short val) {
				short[] masks=FOR_BYTES;
				byte[] result = new byte[masks.length];
				for(short i=0;i<result.length;i++) {
					result[i]=(byte)(val&masks[i]);
				}
				return result;
			}
		}
	}
	
	
	A from(byte value);
	A from(short value);
	A from(int value);
	A from(long value);
	A from(byte[] values);
	A from(short[] values);
	A from(int[] values);
	A from(long[] values);
	byte[] toBytes(A values);
	short[] toShorts(A values);
	int[] toInts(A values);
	long[] toLongs(A values);
	private static <T extends Number> List<T> masksFor(Function<Number,T> converter, IBitUnit from, IBitUnit to){
		int iters = to.per(from);
		List<T> t = new ArrayList<T>(iters);
		long baseMask=~(-1l<<to.bits());
		int shiftAmt = to.bits();
		for(int i=0;i<iters;i++) {
			Long mask = Long.valueOf(baseMask<<i*shiftAmt);
			T squeezed = converter.apply(mask);
			t.add(squeezed);
		}
		return t;
	}
	public static long[] longMasksFor(IBitUnit to) {
		Function<Number,Long> converter = Number::longValue;
		return from(as(masksFor(converter,BitUnit.LONG,to),converter,Long[]::new));
	}
	public static int[] intMasksFor(IBitUnit to) {
		Function<Number,Integer> converter = Number::intValue;
		return from(as(masksFor(converter,BitUnit.INT,to),converter,Integer[]::new));
	}
	public static short[] shortMasksFor(IBitUnit to) {
		Function<Number,Short> converter = Number::shortValue;
		return from(as(masksFor(converter,BitUnit.SHORT,to),converter,Short[]::new));
	}
	public static byte[] byteMasksFor(IBitUnit to) {
		Function<Number,Byte> converter = Number::byteValue;
		return from(as(masksFor(converter,BitUnit.BYTE,to),converter,Byte[]::new));
	}
	
	
	public static void main(String[] args) {
		System.out.println(trimStart(new int[] {1,0,0}, int[]::new));
//		System.out.println(as(masksFor(Number::longValue, BitUnit.LONG, BitUnit.BYTE), Number::longValue,Long[]::new));
//		System.out.println(BinaryStrings.toBinaryString(masksFor(Number::longValue, BitUnit.LONG, BitUnit.INT)));
//		System.out.println(BinaryStrings.toBinaryString(masksFor(Number::intValue, BitUnit.INT, BitUnit.BYTE)));
//		System.out.println(BinaryStrings.toBinaryString(masksFor(Number::shortValue, BitUnit.SHORT, BitUnit.BYTE)));
	}
	private static <T extends Number> T[] as(Collection<T> nums, Function<Number,T> converter, IntFunction<T[]> ctor) {
		return nums.stream().map(converter).toArray(ctor);
	}
	private static byte[] from(Byte[] arr) {
		byte[] b=new byte[arr.length];
		for(int i=0;i<arr.length;i++) {
			b[i]=arr[i];
		}
		return b;
	}
	private static short[] from(Short[] arr) {
		short[] b=new short[arr.length];
		for(int i=0;i<arr.length;i++) {
			b[i]=arr[i];
		}
		return b;
	}
	private static int[] from(Integer[] arr) {
		int[] b=new int[arr.length];
		for(int i=0;i<arr.length;i++) {
			b[i]=arr[i];
		}
		return b;
	}
	private static long[] from(Long[] arr) {
		long[] b=new long[arr.length];
		for(int i=0;i<arr.length;i++) {
			b[i]=arr[i];
		}
		return b;
	}
	public static <A> A[] trim(A[] as, IntFunction<A> ctor) {
		as[0]=trimStart(as[0], ctor);
		as[as.length-1]=trimEnd(as[as.length-1],ctor);
		return as;
	}
	private static <A> A trimStart(A a, IntFunction<A> ctor){
		int len = Array.getLength(a);
		boolean isZero=true;
		int zeros=0;
		for(int i=len-1;isZero&&i<len;i--) {
			Object o = Array.get(a, i);
			isZero=Objects.equals(o, 0);
			if(isZero) {
				zeros+=1;
			}
		}
		if(zeros==0) {
			return a;
		}
		A trimmed = ctor.apply(len-zeros);
		System.arraycopy(a, 0, trimmed, 0, len-zeros);
		
		return trimmed;
	}
	private static <A> A trimEnd(A a, IntFunction<A> ctor){
		int len = Array.getLength(a);
		boolean isZero=true;
		int zeros=0;
		for(int i=len-0;isZero&&i<len;i++) {
			Object o = Array.get(a, i);
			isZero=Objects.equals(o, 0);
			if(isZero) {
				zeros+=1;
			}
		}
		if(zeros==0) {
			return a;
		}
		A trimmed = ctor.apply(len-zeros);
		System.arraycopy(a, len-zeros, trimmed, 0, len-zeros);
		
		return trimmed;
	}
	
	
	
}
