package com.jamesratzlaff.util.bit;

public enum BitUnit implements IBitUnit {
	BYTE(Byte.SIZE),SHORT(Short.SIZE),INT(Integer.SIZE),LONG(Long.SIZE);
	
	
	private final int bits;
	private final int logBase2;
	private final int limitMask;
	
	private BitUnit(int bits) {
		this.bits=bits;
		this.logBase2=IBitUnit.super.log2();
		this.limitMask=IBitUnit.super.limitMask();
	}

	@Override
	public int bits() {
		return this.bits;
	}

	@Override
	public int limitMask() {
		return this.limitMask;
	}

	@Override
	public int log2() {
		return this.logBase2;
	}
	
	public static class ArraySplittersAndJoiners {
		public static class BYTE {
			public static byte[] toBytes(long[] longs) {
				return null;
			}
			public static byte[] toBytes(int[] ints) {
				return null;
			}
			public static byte[] toBytes(short[] shorts) {
				return null;
			}
			
		
			public static short[] toShorts(byte[] bytes) {
				return null;
			}
			public static int[] toInts(byte[] bytes) {
				return null;
			}
			public static long[] toLongs(byte[] bytes) {
				return null;
			}
		}
		public static class SHORT {
			
			public static short[] toShorts(long[] longs) {
				return null;
			}
			public static short[] toShorts(int[] ints) {
				return null;
			}
			public static short[] toShorts(byte[] bytes) {
				return ArraySplittersAndJoiners.BYTE.toShorts(bytes);
			}
			public static byte[] toBytes(short[] shorts) {
				return ArraySplittersAndJoiners.BYTE.toBytes(shorts);
			}
			public static int[] toInts(short[] shorts) {
				return null;
			}
			
			public static long[] toLongs(short[] shorts) {
				return null;
			}
		}
		public static class INT {
			public static int[] toInts(long[] longs) {
				return null;
			}
		}
	}
		
}
