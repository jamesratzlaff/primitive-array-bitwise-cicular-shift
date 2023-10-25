package com.jamesratzlaff.util.bit.converters;

import com.jamesratzlaff.util.bit.BitUnit;
import com.jamesratzlaff.util.bit.IBitUnit;
import com.jamesratzlaff.util.bit.IntergralArrayConversion;

public class ShortArrayConverter implements IntergralArrayConversion<short[]>{
		
		
		@Override
		public short[] from(byte[] values) {
			return BitUnit.ArrayConverters.BYTE.toShorts(values);
		}

		@Override
		public short[] from(short[] values) {
			return values;
		}

		@Override
		public short[] from(int[] values) {
			int scalar = BitUnit.BYTE.per(BitUnit.INT);
			int len = scalar*values.length;
			short[] result = new short[len];
			for(int i=0;i<values.length;i++) {
				int value = values[i];
				short[] vals = from(value);
				System.arraycopy(vals, 0, result, i*scalar, scalar);
			}
			return result;
		}

		@Override
		public short[] from(long[] values) {
			int scalar = BitUnit.SHORT.per(BitUnit.LONG);
			int len = scalar*values.length;
			short[] result = new short[len];
			for(int i=0;i<values.length;i++) {
				long value = values[i];
				short[] vals = from(value);
				System.arraycopy(vals, 0, result, i*scalar, scalar);
			}
			return result;
		}

		

		@Override
		public short[] toShorts(short[] values) {
			return values;
		}

		@Override
		public int[] toInts(short[] values) {
			IBitUnit unit = BitUnit.SHORT;
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
		public long[] toLongs(short[] values) {
			IBitUnit unit = BitUnit.SHORT;
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
		private short toShort(short[] values, int startIdx) {
			IBitUnit unit = BitUnit.BYTE;
			int bits = unit.bits();
			short result=0;
			int iterationsLeft=values.length-startIdx;
			int endIdx = Math.min(iterationsLeft, startIdx+1);
			for(int i=0;i<endIdx;i++) {
				int idx = startIdx+i;
				short value=values[idx];
				result|=(value<<(i*bits));
			}
			return result;
		}
		private int toInt(short[] values, int startIdx) {
			IBitUnit unit = BitUnit.SHORT;
			int bits = unit.bits();
			int numUnits=unit.per(BitUnit.INT);
			int result=0;
			int iterationsLeft=values.length-startIdx;
			int endIdx = Math.min(iterationsLeft, numUnits);
			for(int i=0;i<endIdx;i++) {
				int idx = startIdx+i;
				short value=values[idx];
				result|=(value<<(i*bits));
			}
			return result;
		}
		private long toLong(short[] values, int startIdx) {
			IBitUnit unit = BitUnit.SHORT;
			int bits = unit.bits();
			int numUnits=unit.per(BitUnit.LONG);
			long result=0l;
			int iterationsLeft=values.length-startIdx;
			int endIdx = Math.min(iterationsLeft, numUnits);
			for(int i=0;i<endIdx;i++) {
				int idx = startIdx+i;
				short value=values[idx];
				result|=(value<<(i*bits));
			}
			return result;
		}
		@Override
		public short[] from(byte value) {
			return new short[] {(short)(0xFF&(int)value)};
		}

		@Override
		public short[] from(short value) {
			return  new short[] {value};
		}


		@Override
		public short[] from(int value) {
			return IntergralArrayConversion.MASKS.INT.toShorts(value);
		}


		@Override
		public short[] from(long value) {
			return IntergralArrayConversion.MASKS.LONG.toShorts(value);
		}

		@Override
		public byte[] toBytes(short[] values) {
			return BitUnit.ArrayConverters.BYTE.from(values);
		}
}
