package com.jamesratzlaff.util.bit.converters;

import com.jamesratzlaff.util.bit.BitUnit;
import com.jamesratzlaff.util.bit.IBitUnit;
import com.jamesratzlaff.util.bit.IntergralArrayConversion;

public class IntArrayConverter implements IntergralArrayConversion<int[]>{
	
	
	@Override
	public int[] from(byte[] values) {
		return BitUnit.ArrayConverters.BYTE.toInts(values);
	}

	@Override
	public int[] from(short[] values) {
		return BitUnit.ArrayConverters.SHORT.toInts(values);
	}

	@Override
	public int[] from(int[] values) {
		return values;
	}

	@Override
	public int[] from(long[] values) {
		int scalar = BitUnit.INT.per(BitUnit.LONG);
		int len = scalar*values.length;
		int[] result = new int[len];
		for(int i=0;i<values.length;i++) {
			long value = values[i];
			int[] vals = from(value);
			System.arraycopy(vals, 0, result, i*scalar, scalar);
		}
		return result;
	}

	

	@Override
	public short[] toShorts(int[] values) {
		return BitUnit.ArrayConverters.SHORT.from(values);
	}

	@Override
	public int[] toInts(int[] values) {
		return values;
	}

	@Override
	public long[] toLongs(int[] values) {
		IBitUnit unit = BitUnit.INT;
		int numUnits=unit.per(BitUnit.LONG);
		int arrLen = values.length/numUnits;
		if(values.length-(arrLen*numUnits)>0) {
			arrLen+=1;
		}
		long[] result=new long[arrLen];
		for(int i=0;i<arrLen;i++) {
			long l = toLong(values, i*numUnits);
			result[i]=l;
		}
		return result;
	}
	
	private long toLong(int[] values, int startIdx) {
		IBitUnit unit = BitUnit.INT;
		int bits = unit.bits();
		int numUnits=unit.per(BitUnit.LONG);
		long result=0l;
		int iterationsLeft=values.length-startIdx;
		int endIdx = Math.min(iterationsLeft, numUnits);
		for(int i=0;i<endIdx;i++) {
			int idx = startIdx+i;
			int value=values[idx];
			result|=(value<<(i*bits));
		}
		return result;
	}
	@Override
	public int[] from(byte value) {
		return new int[] {0xFF&(int)value};
	}

	@Override
	public int[] from(short value) {
		return  new int[] {(int)(0xFFFF&value)};
	}


	@Override
	public int[] from(int value) {
		return new int[] {value};
	}


	@Override
	public int[] from(long value) {
		return IntergralArrayConversion.MASKS.LONG.toInts(value);
	}

	@Override
	public byte[] toBytes(int[] values) {
		return BitUnit.ArrayConverters.BYTE.from(values);
	}
}
