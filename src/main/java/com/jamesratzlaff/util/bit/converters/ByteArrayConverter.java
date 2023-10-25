package com.jamesratzlaff.util.bit.converters;

import com.jamesratzlaff.util.bit.BitUnit;
import com.jamesratzlaff.util.bit.IBitUnit;
import com.jamesratzlaff.util.bit.IntergralArrayConversion;

public class ByteArrayConverter implements IntergralArrayConversion<byte[]>{
	
	
	@Override
	public byte[] from(byte value) {
		return new byte[] {value};
	}
	
	
	@Override
	public byte[] from(byte[] values) {
		return values;
	}

	@Override
	public byte[] from(short[] values) {
		int scalar = BitUnit.BYTE.per(BitUnit.SHORT);
		int len = scalar*values.length;
		byte[] result = new byte[len];
		for(int i=0;i<values.length;i++) {
			short value = values[i];
			byte[] vals = from(value);
			System.arraycopy(vals, 0, result, i*scalar, scalar);
		}
		return result;
	}

	@Override
	public byte[] from(int[] values) {
		int scalar = BitUnit.BYTE.per(BitUnit.INT);
		int len = scalar*values.length;
		byte[] result = new byte[len];
		for(int i=0;i<values.length;i++) {
			int value = values[i];
			byte[] vals = from(value);
			System.arraycopy(vals, 0, result, i*scalar, scalar);
		}
		return result;
	}

	@Override
	public byte[] from(long[] values) {
		int scalar = BitUnit.BYTE.per(BitUnit.LONG);
		int len = scalar*values.length;
		byte[] result = new byte[len];
		for(int i=0;i<values.length;i++) {
			long value = values[i];
			byte[] vals = from(value);
			System.arraycopy(vals, 0, result, i*scalar, scalar);
		}
		return result;
	}

	@Override
	public byte[] toBytes(byte[] values) {
		return values;
	}

	@Override
	public short[] toShorts(byte[] values) {
		IBitUnit unit = BitUnit.BYTE;
		int numUnits=unit.per(BitUnit.SHORT);
		int arrLen = values.length/numUnits;
		if(values.length-(arrLen*numUnits)>0) {
			arrLen+=1;
		}
		short[] result=new short[arrLen];
		for(int i=0;i<arrLen;i++) {
			short l = toShort(values, i*numUnits);
			result[i]=l;
		}
		return result;
	}

	@Override
	public int[] toInts(byte[] values) {
		IBitUnit unit = BitUnit.BYTE;
		int numUnits=unit.per(BitUnit.INT);
		int arrLen = values.length/numUnits;
		if(values.length-(arrLen*numUnits)>0) {
			arrLen+=1;
		}
		int[] result=new int[arrLen];
		for(int i=0;i<arrLen;i++) {
			int l = toInt(values, i*numUnits);
			result[i]=l;
		}
		return result;
	}

	@Override
	public long[] toLongs(byte[] values) {
		IBitUnit unit = BitUnit.BYTE;
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
	private short toShort(byte[] values, int startIdx) {
		IBitUnit unit = BitUnit.BYTE;
		int bits = unit.bits();
		int numUnits=unit.per(BitUnit.SHORT);
		short result=0;
		int iterationsLeft=values.length-startIdx;
		int endIdx = Math.min(iterationsLeft, numUnits);
		for(int i=0;i<endIdx;i++) {
			int idx = startIdx+i;
			byte value=values[idx];
			result|=(value<<(i*bits));
		}
		return result;
	}
	private int toInt(byte[] values, int startIdx) {
		IBitUnit unit = BitUnit.BYTE;
		int bits = unit.bits();
		int numUnits=unit.per(BitUnit.INT);
		int result=0;
		int iterationsLeft=values.length-startIdx;
		int endIdx = Math.min(iterationsLeft, numUnits);
		for(int i=0;i<endIdx;i++) {
			int idx = startIdx+i;
			byte value=values[idx];
			result|=(value<<(i*bits));
		}
		return result;
	}
	private long toLong(byte[] values, int startIdx) {
		IBitUnit unit = BitUnit.BYTE;
		int bits = unit.bits();
		int numUnits=unit.per(BitUnit.LONG);
		long result=0l;
		int iterationsLeft=values.length-startIdx;
		int endIdx = Math.min(iterationsLeft, numUnits);
		for(int i=0;i<endIdx;i++) {
			int idx = startIdx+i;
			byte value=values[idx];
			result|=(value<<(i*bits));
		}
		return result;
	}


	@Override
	public byte[] from(short value) {
		return  IntergralArrayConversion.MASKS.SHORT.toBytes(value);
	}


	@Override
	public byte[] from(int value) {
		return IntergralArrayConversion.MASKS.INT.toBytes(value);
	}


	@Override
	public byte[] from(long value) {
		return IntergralArrayConversion.MASKS.LONG.toBytes(value);
	}

}
