package com.jamesratzlaff.util.bit.converters;

import com.jamesratzlaff.util.bit.BitUnit;
import com.jamesratzlaff.util.bit.IntergralArrayConversion;

public class LongArrayConverter implements IntergralArrayConversion<long[]>{
	
	
	@Override
	public long[] from(byte[] values) {
		return BitUnit.ArrayConverters.BYTE.toLongs(values);
	}

	@Override
	public long[] from(short[] values) {
		return BitUnit.ArrayConverters.SHORT.toLongs(values);
	}

	@Override
	public long[] from(int[] values) {
		return BitUnit.ArrayConverters.INT.toLongs(values);
	}
	
	

	@Override
	public long[] from(long[] values) {
		return values;
	}

	

	@Override
	public short[] toShorts(long[] values) {
		return BitUnit.ArrayConverters.SHORT.from(values);
	}

	@Override
	public int[] toInts(long[] values) {
		return BitUnit.ArrayConverters.INT.from(values);
	}

	@Override
	public long[] toLongs(long[] values) {
		return values;
	}
	
	
	@Override
	public long[] from(byte value) {
		return new long[] {0xFF&(long)value};
	}

	@Override
	public long[] from(short value) {
		return  new long[] {(long)(0xFFFF&value)};
	}


	@Override
	public long[] from(int value) {
		return new long[] {0xFFFFFFFF&value};
	}


	@Override
	public long[] from(long value) {
		return new long[] {value};
	}

	@Override
	public byte[] toBytes(long[] values) {
		return BitUnit.ArrayConverters.BYTE.from(values);
	}
}