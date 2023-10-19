package com.jamesratzlaff.util.bit;


public class IntArrayShift {

	private static final BitUnit unit = BitUnit.INT;
	/**
	 * 
	 * @param bits an <code>int[]</code> that represents bits
	 * @param size the number of <i>bit</i> this <code>int[]</code> represents
	 * @param amt the amount to rotate the bits, a positive value will rotate right, a negative value will rotate left
	 * @return a copy of the passed in <code>bits</code> parameter with the copy's bits rotated
	 */
	public static int[] nonMutatingBitwiseRotate(int[] bits, int size, int amt) {
		int[] copy = new int[bits.length];
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
	public static int[] bitwiseRotate(int[] bits, int size, int amt) {
		amt = normalizeCyclic(amt, size);
		int unitShifts = amt >>> unit.multOrDivShift();
		amt -= (unitShifts << unit.multOrDivShift());
		int maskShifts = unit.bits() - amt;
		int carryMask = maskShifts!=unit.bits()?(-1 >>> maskShifts):0;
		int lastCarryMask = carryMask;
		int lastIndexBits = size & unit.limitMask();
		final int lastCarryMaskShift = unit.bits()-lastIndexBits;
		int bleedOverSize = amt - lastIndexBits;//lastCarryMaskLen;
		if (bleedOverSize < 0) {
			bleedOverSize = 0;
		}
		if (lastIndexBits != 0) {
			lastCarryMask = maskShifts!=unit.bits()?(-1 >>> Math.max(lastCarryMaskShift, maskShifts)):0;
		}
		int lastSegmentIndex = bits.length - 1;
		int shiftedLastSegmentIndex = normalizeCyclic(unitShifts + lastSegmentIndex, bits.length);
		bits = rotate(bits, unitShifts);
		
		if (shiftedLastSegmentIndex != lastSegmentIndex) {
			
			int prevIdxGrabMask = ~(-1>>>(lastCarryMaskShift)) >>> lastIndexBits;
			for (int i = 0; i < bits.length - 1; i++) {
				int currentIndex = normalizeCyclic(shiftedLastSegmentIndex - i, bits.length);
				if(currentIndex==bits.length-1) {
					break;
				}
				int prevIndex = normalizeCyclic(currentIndex - 1, bits.length);
				int orVal = bits[prevIndex];
				orVal &= prevIdxGrabMask;
				orVal = orVal << lastIndexBits;
				bits[currentIndex] |= orVal;
				bits[prevIndex] = bits[prevIndex] >>> lastCarryMaskShift;
				

			}
		}
		int[] orVals=new int[bits.length];
		for (int i = 0; i < bits.length; i++) {
			int mask = carryMask;
			int reshift = maskShifts;
			int nextUnit=i+1;
			if (i == lastSegmentIndex) {
				mask = lastCarryMask;
				reshift = Integer.numberOfLeadingZeros(lastCarryMask);//carryMaskReShiftLen;
				nextUnit=0;
			}
			if(i==lastSegmentIndex-1) {
				if(amt<lastIndexBits) {
					reshift=lastIndexBits-(amt%lastIndexBits);
				} else {
					reshift=0;
				}
			}
			int orVal = mask & bits[i];
			orVal = orVal << reshift;
			orVals[nextUnit] = orVal;
		}
		
		if(bleedOverSize>0) {
			int bleedOverShifts=unit.bits()-bleedOverSize;
			int bleedGrabMask=(-1>>>bleedOverShifts);
			int grabbedValue=orVals[lastSegmentIndex]&bleedGrabMask;
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
	 * @param array the <code>int[]</code> of values to rotate 
	 * @param amt the amount to rotate values contained in the <code>array</code> parameter, positive values rotate to the right, negative values rotate to the left
	 * @return the given <code>array</cod> parameter with each int value rotated
	 */
	public static int[] rotate(int[] array, int amt) {
		int[] reso = nonMutatingRotate(array, amt);
		System.arraycopy(reso, 0, array, 0, array.length);
		return array;
	}

	/**
	 * 
	 * @param array the <code>int[]</code> of values to rotate 
	 * @param amt the amount to rotate values contained in the <code>array</code> parameter, positive values rotate to the right, negative values rotate to the left
	 * @return a copy of the given <code>array</cod> parameter with each int value rotated
	 */
	public static int[] nonMutatingRotate(int[] array, int amt) {
		amt = normalizeCyclic(amt, array.length);
		if (amt > 0) {
			int[] result = new int[array.length];
			int toIdx = array.length - amt;
			System.arraycopy(array, toIdx, result, 0, amt);
			System.arraycopy(array, 0, result, amt, toIdx);
			return result;
		}
		return array;
	}
	
	private static int normalizeCyclic(int value, int bound) {
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
