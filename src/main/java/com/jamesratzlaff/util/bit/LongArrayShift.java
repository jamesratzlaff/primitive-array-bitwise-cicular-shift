package com.jamesratzlaff.util.bit;

public class LongArrayShift {

	private static final BitUnit unit = BitUnit.LONG;
	/**
	 * 
	 * @param bits an <code>long[]</code> that represents bits
	 * @param size the number of <i>bit</i> this <code>long[]</code> represents
	 * @param amt the amount to rotate the bits, a positive value will rotate right, a negative value will rotate left
	 * @return a copy of the passed in <code>bits</code> parameter with the copy's bits rotated
	 */
	public static long[] nonMutatingBitwiseRotate(long[] bits, long size, long amt) {
		long[] copy = new long[bits.length];
		System.arraycopy(bits, 0, copy, 0, bits.length);
		return bitwiseRotate(copy, size, amt);
	}

	/**
	 * 
	 * @param bits an <code>int[]</code> that represents bits
	 * @param size the number of <i>bit</i> this <code>int[]</code> represents
	 * @param amt the amount to rotate the bits, a positive value will rotate right, a negative value will rotate left
	 * @return the passed in <code>bits</code> parameter with its bits rotated
	 */
	public static long[] bitwiseRotate(long[] bits, long size, long amt) {
		amt = normalizeCyclic(amt, size);
		long unitShifts = amt >>> unit.multOrDivShift();
		amt -= (unitShifts << unit.multOrDivShift());
		long maskShifts = unit.bits() - amt;
		long carryMask = maskShifts!=unit.bits()?(-1 >>> maskShifts):0;
		long lastCarryMask = carryMask;
		long lastIndexBits = size & unit.limitMask();
		final long lastCarryMaskShift = unit.bits()-lastIndexBits;
		long bleedOverSize = amt - lastIndexBits;//lastCarryMaskLen;
		if (bleedOverSize < 0) {
			bleedOverSize = 0;
		}
		if (lastIndexBits != 0) {
			lastCarryMask = maskShifts!=unit.bits()?(-1 >>> Math.max(lastCarryMaskShift, maskShifts)):0;
		}
		int lastSegmentIndex = bits.length - 1;
		long shiftedLastSegmentIndex = normalizeCyclic(unitShifts + lastSegmentIndex, bits.length);
		bits = rotate(bits, (int)unitShifts);
		
		if (shiftedLastSegmentIndex != lastSegmentIndex) {
			
			long prevIdxGrabMask = ~(-1>>>(lastCarryMaskShift)) >>> lastIndexBits;
			for (long i = 0; i < bits.length - 1; i++) {
				int currentIndex = normalizeCyclicI(shiftedLastSegmentIndex - i, bits.length);
				if(currentIndex==bits.length-1) {
					break;
				}
				int prevIndex = normalizeCyclicI(currentIndex - 1, bits.length);
				long orVal = bits[prevIndex];
				orVal &= prevIdxGrabMask;
				orVal = orVal << lastIndexBits;
				bits[currentIndex] |= orVal;
				bits[prevIndex] = bits[prevIndex] >>> lastCarryMaskShift;
				

			}
		}
		long[] orVals=new long[bits.length];
		for (int i = 0; i < bits.length; i++) {
			long mask = carryMask;
			long reshift = maskShifts;
			int nextUnit=i+1;
			if (i == lastSegmentIndex) {
				mask = lastCarryMask;
				reshift = Long.numberOfLeadingZeros(lastCarryMask);//carryMaskReShiftLen;
				nextUnit=0;
			}
			if(i==lastSegmentIndex-1) {
				if(amt<lastIndexBits) {
					reshift=lastIndexBits-(amt%lastIndexBits);
				} else {
					reshift=0;
				}
			}
			long orVal = mask & bits[i];
			orVal = orVal << reshift;
			orVals[nextUnit] = orVal;
		}
		
		if(bleedOverSize>0) {
			long bleedOverShifts=unit.bits()-bleedOverSize;
			long bleedGrabMask=(-1>>>bleedOverShifts);
			long grabbedValue=orVals[lastSegmentIndex]&bleedGrabMask;
			orVals[lastSegmentIndex]=orVals[lastSegmentIndex]>>>bleedOverSize;
			orVals[0]=orVals[0]>>>bleedOverSize;
			grabbedValue=(grabbedValue<<bleedOverShifts);
			orVals[0]|=grabbedValue;
		}
		
		for(int i=0;i<bits.length;i++) {
			bits[i]=(bits[i]>>>amt)|orVals[i];
		}
		return bits;
	}
	/**
	 * 
	 * @param array the <code>long[]</code> of values to rotate 
	 * @param amt the amount to rotate values contained in the <code>array</code> parameter, positive values rotate to the right, negative values rotate to the left
	 * @return the given <code>array</cod> parameter with each int value rotated
	 */
	public static long[] rotate(long[] array, int amt) {
		long[] reso = nonMutatingRotate(array, amt);
		System.arraycopy(reso, 0, array, 0, array.length);
		return array;
	}

	/**
	 * 
	 * @param array the <code>long[]</code> of values to rotate 
	 * @param amt the amount to rotate values contained in the <code>array</code> parameter, positive values rotate to the right, negative values rotate to the left
	 * @return a copy of the given <code>array</cod> parameter with each int value rotated
	 */
	public static long[] nonMutatingRotate(long[] array, int amt) {
		amt = (int)normalizeCyclic(amt, array.length);
		if (amt > 0) {
			long[] result = new long[array.length];
			int toIdx = array.length - amt;
			System.arraycopy(array, toIdx, result, 0, amt);
			System.arraycopy(array, 0, result, amt, toIdx);
			return result;
		}
		return array;
	}
	private static int normalizeCyclicI(long value, long bound) {
		return (int)normalizeCyclic(value, bound);
	}	
	private static long normalizeCyclic(long value, long bound) {
		if (value < 0) {
			if (-value > bound) {
				if (bound == unit.bits()) {
					value = value & unit.limitMask();
				} else {
					value %= bound;
				}
			}
			value = bound + value;
		}
		if (value == bound) {
			return 0;
		}
		if (value > bound) {
			if (bound == unit.bits()) {
				value = value & unit.limitMask();
			} else {
				value %= bound;
			}
		}

		return value;
	}
	
}
