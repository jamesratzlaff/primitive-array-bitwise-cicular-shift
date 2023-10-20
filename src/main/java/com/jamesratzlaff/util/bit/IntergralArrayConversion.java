package com.jamesratzlaff.util.bit;

public interface IntergralArrayConversion<A> {

	A from(byte[] values);
	A from(short[] values);
	A from(int[] values);
	A from(long[] values);
	byte[] toBytes(A values);
	short[] toShorts(A values);
	int[] toInts(A values);
	long[] toLongs(A values);
	
	
}
