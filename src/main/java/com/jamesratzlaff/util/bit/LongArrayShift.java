package com.jamesratzlaff.util.bit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LongArrayShift {

	private static final BitUnit unit = BitUnit.LONG;

	/**
	 * 
	 * @param bits an <code>long[]</code> that represents bits
	 * @param size the number of <i>bit</i> this <code>long[]</code> represents
	 * @param amt  the amount to rotate the bits, a positive value will rotate
	 *             right, a negative value will rotate left
	 * @return a copy of the passed in <code>bits</code> parameter with the copy's
	 *         bits rotated
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
	 * @param amt  the amount to rotate the bits, a positive value will rotate
	 *             right, a negative value will rotate left
	 * @return the passed in <code>bits</code> parameter with its bits rotated
	 */
	public static long[] bitwiseRotate(long[] bits, long size, long amt) {
		amt = normalizeCyclic(amt, size);
		long unitShifts = amt >>> unit.multOrDivShift();
		amt -= (unitShifts << unit.multOrDivShift());
		long maskShifts = normalizeCyclic(unit.bits() - amt, unit.bits());
		long carryMask = maskShifts != unit.bits() ? (-1l >>> maskShifts) : 0l;
		long lastCarryMask = carryMask;
		long lastIndexBits = size & unit.limitMask();
		final long lastCarryMaskShift = unit.bits() - lastIndexBits;
		long bleedOverSize = amt - lastIndexBits;// lastCarryMaskLen;
		if (bleedOverSize < 0) {
			bleedOverSize = 0l;
		}
		if (lastIndexBits != 0) {
			lastCarryMask = maskShifts != unit.bits() ? (-1l >>> Math.max(lastCarryMaskShift, maskShifts)) : 0l;
		}
		int lastSegmentIndex = bits.length - 1;
		long shiftedLastSegmentIndex = normalizeCyclic(unitShifts + lastSegmentIndex, bits.length);
		bits = rotate(bits, (int) unitShifts);

		if (shiftedLastSegmentIndex != lastSegmentIndex) {

			long prevIdxGrabMask = ~(-1 >>> (lastCarryMaskShift)) >>> lastIndexBits;
			for (long i = 0; i < bits.length - 1; i++) {
				int currentIndex = normalizeCyclicI(shiftedLastSegmentIndex - i, bits.length);
				if (currentIndex == bits.length - 1) {
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
		long[] orVals = new long[bits.length];
		for (int i = 0; i < bits.length; i++) {
			long mask = carryMask;
			long reshift = maskShifts;
			int nextUnit = i + 1;
			if (i == lastSegmentIndex) {
				mask = lastCarryMask;
				reshift = Long.numberOfLeadingZeros(lastCarryMask);// carryMaskReShiftLen;
				nextUnit = 0;
			}
			if (i == lastSegmentIndex - 1) {
				if (amt < lastIndexBits) {
					reshift = lastIndexBits - (amt % lastIndexBits);
				} else {
					reshift = 0;
				}
			}
			long orVal = mask & bits[i];
			orVal = orVal << reshift;
			orVals[nextUnit] = orVal;
		}

		if (bleedOverSize > 0) {
			long bleedOverShifts = unit.bits() - bleedOverSize;
			long bleedGrabMask = (-1l >>> bleedOverShifts);
			long grabbedValue = orVals[lastSegmentIndex] & bleedGrabMask;
			orVals[lastSegmentIndex] = orVals[lastSegmentIndex] >>> bleedOverSize;
			orVals[0] = orVals[0] >>> bleedOverSize;
			grabbedValue = (grabbedValue << bleedOverShifts);
			orVals[0] |= grabbedValue;
		}

		for (int i = 0; i < bits.length; i++) {
			long newVal = (bits[i] >>> amt) | orVals[i];
//			LongArrayShift.printB(newVal);
//			newVal=Long.reverse(newVal);
//			LongArrayShift.printB(newVal);
			bits[i] = newVal;

		}
		return bits;
	}

	private static void printB(long val) {
		System.out.println(toBin(val));
	}

	private static String toBin(long val) {
		return toBin(null, null, val);
	}

	private static void printB(String label, long val) {
		System.out.println(toBin(label, val));
	}

	private static String toBin(String label, long val) {
		return toBin(label, null, val);
	}

	private static void printB(String label, String delim, long val) {
		System.out.println(toBin(label, delim, val));
	}

	private static String toBin(String label, String delim, long val) {
		StringBuilder sb = new StringBuilder();
		if (label != null) {
			sb.append(label);
			if (!label.endsWith(":")) {
				sb.append(':');
			}
			if (delim != null) {
				sb.append(delim);
			} else {
				sb.append("\t");
			}
		}
		sb.append(BinaryStrings.toBinaryString(val));
		return sb.toString();
	}

	/**
	 * This may have to become rotateAndCollapseForRightShift since the bitArray
	 * representation of the number arrays is represented backwards
	 * 
	 * @param bits
	 * @param size
	 * @param shiftUnits
	 * @return
	 */
	private static long[] rotateAndCollapseForLeftShift(long[] bits, int size, int shiftUnits) {
		rotate(bits, shiftUnits);
//		System.out.println("rotated:\t\t"+BinaryStrings.toBinaryString(bits,size));
		// the last bit set to carry from should be bits.length-1;
		int lastIndex = bits.length - 1;
		int newLastBitsIndex = normalizeCyclicI(lastIndex + shiftUnits, bits.length);
		int numberOfLastIndexBits = size & unit.limitMask();
		if (numberOfLastIndexBits == 0) {
			return bits;
		}
		long lastIndexCarryMask = -1l << numberOfLastIndexBits;
		int numberOfShifts = unit.bits() - numberOfLastIndexBits;
		// should come out to be 9,9,4;
		for (int i = 0; i < bits.length; i++) {
			int currentIndex = normalizeCyclicI(newLastBitsIndex - i, bits.length);
			int prevIndex = normalizeCyclicI(currentIndex - 1, bits.length);
			if (currentIndex == lastIndex) {
				break;
			}
			long currentValue = bits[currentIndex];
//			System.out.println("currentValue:\t\t"+BinaryStrings.toBinaryString(currentValue));
			currentValue <<= numberOfShifts;
//			System.out.println("currentValue:\t\t"+BinaryStrings.toBinaryString(currentValue));
			long prevValue = bits[prevIndex];
//			System.out.println("prevValue:\t\t"+BinaryStrings.toBinaryString(prevValue));
			long orVal = prevValue & lastIndexCarryMask;
			orVal >>>= numberOfLastIndexBits;
//			System.out.println("orVal:\t\t\t"+BinaryStrings.toBinaryString(orVal));
			long newPrevValueMask = ~(-1l << numberOfLastIndexBits);
//			System.out.println("newPrevValueMask:\t"+BinaryStrings.toBinaryString(newPrevValueMask));
			prevValue &= newPrevValueMask;
//			System.out.println("prevValue:\t\t"+BinaryStrings.toBinaryString(prevValue));
			currentValue |= orVal;
			bits[currentIndex] = currentValue;
			bits[prevIndex] = prevValue;
		}
		return bits;

	}

	private static long[] getOrValsForLeftShift(long[] bits, int size, int amt) {
		int lastIndex = bits.length - 1;
		int numberOfLastIndexBits = size & unit.limitMask();
		long numberOfCarryBits = unit.bits() - amt;
		long carryMask = ~(-1l >>> numberOfCarryBits);
		long lastIndexCarryMask = amt >= numberOfLastIndexBits ? ~(-1l << numberOfLastIndexBits)
				: -1l << (numberOfLastIndexBits - amt);
		long bleedOverMask = amt >= numberOfLastIndexBits ? ~(-1l >>> (amt - numberOfLastIndexBits)) : 0;
		int numberOfShifts = unit.bits()-amt;
		long[] orVals = new long[bits.length];
		for (int i = 0; i < bits.length; i++) {
			int currentIndex = i;
			if (currentIndex == lastIndex) {
				long orVal=bits[currentIndex];
				orVal&=lastIndexCarryMask;
				orVal<<=unit.bits()-numberOfLastIndexBits;
				if(currentIndex!=0) {
					if(bleedOverMask!=0) {
						long bleedOver=orVals[currentIndex-1];
						bleedOver<<=numberOfShifts;
						long bleedOverVal=bleedOverMask&bleedOverMask;
						bleedOverVal>>>=numberOfLastIndexBits;
						orVal|=bleedOverVal;
						bleedOver&=~bleedOverMask;
						bleedOver>>>=numberOfShifts;
						orVals[currentIndex-1]=bleedOver;
					}
				}
				orVal >>>=numberOfShifts;
				orVals[i]=orVal;
			} else {
				long currentValue = bits[currentIndex];
				long orVal = currentValue & carryMask;
				orVal >>>= numberOfShifts;
				orVals[i] = orVal;
			}
		}
		return orVals;

	}
	
	private static long[] shiftLeft(long[] bits, int size, int amt) {
		amt = normalizeCyclicI(amt, size);
		int unitShifts = amt >>> unit.multOrDivShift();
		amt -= (unitShifts << unit.multOrDivShift());
		if(unitShifts>0) {
			rotateAndCollapseForLeftShift(bits, size, unitShifts);
		}
		long[] orVals = getOrValsForLeftShift(bits, size, amt);
		rotate(orVals, 1);
		for(int i=0;i<bits.length;i++) {
			bits[i]=(bits[i]<<amt|orVals[i]);
		}
		return bits;
	}
	

//	private static long[] getOrValsForLeftShift(long[] bits, long amt, int size) {
//		long[] orVals = new long[bits.length];
//		long lastIndexBits = size & unit.limitMask();
//		long numberOfCarryBits = unit.bits() - amt;
//		long carryMask = -1l << numberOfCarryBits;
//		long lastCarryShifts = unit.bits() - lastIndexBits;
//		for (int i = 0; i < bits.length; i++) {
//			long currentVal = bits[i];
//			long maskedVal = carryMask & bits[i];
//			if (i == bits.length - 1) {
//				currentVal <<= lastCarryShifts;
//				System.out.println(BinaryStrings.toBinaryString(currentVal));
//			}
//		}
//		return null;
//
//	}

	private static String toBin(String label, String delim, long[] val) {
		return toBin(label, delim, val, -1);
	}

	private static String toBin(String label, String delim, long[] val, int size) {
		StringBuilder sb = new StringBuilder();
		if (label != null) {
			sb.append(label);
			if (delim != null) {
				sb.append(delim);
			} else {
				sb.append("\t");
			}
		}
		sb.append(BinaryStrings.toBinaryString(val, size));
		return sb.toString();
	}

	/**
	 * 
	 * @param array the <code>long[]</code> of values to rotate
	 * @param amt   the amount to rotate values contained in the <code>array</code>
	 *              parameter, positive values rotate to the right, negative values
	 *              rotate to the left
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
	 * @param amt   the amount to rotate values contained in the <code>array</code>
	 *              parameter, positive values rotate to the right, negative values
	 *              rotate to the left
	 * @return a copy of the given <code>array</cod> parameter with each int value
	 *         rotated
	 */
	public static long[] nonMutatingRotate(long[] array, int amt) {
		amt = (int) normalizeCyclic(amt, array.length);
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
		return (int) normalizeCyclic(value, bound);
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

	public static long[] subBits(long[] bits, int startBit, int endExclusive) {
		int merge = startBit ^ endExclusive;
		startBit = Math.min(merge ^ startBit, merge ^ endExclusive);
		endExclusive = merge ^ startBit;
		int startIdx = getArrayIdxUsingBitIdx(startBit);
		int endIdx = getArrayIdxUsingBitIdx(endExclusive);
		int len = (endIdx - startIdx) + 1;
		long[] reso = new long[len];
		System.arraycopy(bits, startIdx, reso, 0, len);
		int reducedSize = endExclusive - startBit;
		endExclusive = (endExclusive - startBit) & unit.limitMask();
		startBit = startBit & unit.limitMask();
		shiftLeft(reso, startBit);
		reso[len - 1] = reso[len - 1] >>> (unit.bits() - endExclusive);
		return reso;
	}

	private static void shiftLeft(long[] bits, int amt) {
		int size = unit.bits() * bits.length;
		amt = normalizeCyclicI(amt, size);
		long orMask = -1l << (unit.bits() - amt);
		int shiftAmt = amt;
		int numberOfLastBits = size & unit.limitMask();
		long lastMaskSize = Math.min(amt, numberOfLastBits);
		long lastMaskShifts = (unit.bits() - lastMaskSize);
		long lastMask = -1l >>> lastMaskShifts;
		int lastIdx = bits.length - 1;
		int lastIter = lastIdx - 1;
		for (int i = 0; i < bits.length; i++) {
			long current = bits[i];
			current = current << shiftAmt;
			if (i < lastIdx) {
				long orVal = (orMask & bits[i + 1]);
				current |= orVal;
			}
			bits[i] = current;
		}
	}

	private static int getArrayIdxUsingBitIdx(int bitIdx) {
		return bitIdx >>> unit.multOrDivShift();
	}

	private static List<String> getArraySubArrayComparisonStrings(long[] bits, int size, int subStart, int subEnd) {
		List<String> strs = new ArrayList<String>(2);
		strs.add(BinaryStrings.toBinaryString(bits, size, ""));
		char[] spaces = new char[subStart];
		Arrays.fill(spaces, ' ');
		long[] subArray = subBits(bits, subStart, subEnd);
		strs.add(new String(spaces) + BinaryStrings.toBinaryString(subArray, subEnd - subStart, ""));
		return strs;
	}

	private static void printSubArrayComparison(long[] bits, int size, int subStart, int subEnd) {
		List<String> strs = getArraySubArrayComparisonStrings(bits, size, subStart, subEnd);
		System.out.println(String.join("\n", strs));
	}

	private static void shiftUnitsRightAndRejoin(long[] bits, int size, int unitShifts) {
		System.out.println("bits:\t\t" + BinaryStrings.toBinaryString(bits));
		rotate(bits, unitShifts);
		System.out.println("bits:\t\t" + BinaryStrings.toBinaryString(bits));
		int lastSegmentIndex = bits.length - 1;
		System.out.println("lastSegmentIndex:\t\t" + lastSegmentIndex);
		long lastIndexBits = size & unit.limitMask();
		System.out.println("lastIndexBits:\t\t" + BinaryStrings.toBinaryString(lastIndexBits));

		long shiftedLastSegmentIndex = normalizeCyclic(unitShifts + lastSegmentIndex, bits.length);
		System.out.println("shiftedLastSegmentIndex:\t" + shiftedLastSegmentIndex);
		long prevIdxGrabMask = -1l << lastIndexBits;
		System.out.println("prevIdxGrabMask:\t\t" + BinaryStrings.toBinaryString(prevIdxGrabMask));
		for (long i = 0; i < bits.length - 1; i++) {
			System.out.println("===========START========");
			int currentIndex = normalizeCyclicI(shiftedLastSegmentIndex - i, bits.length);
			if (currentIndex == bits.length - 1) {
				break;
			}
			System.out.println("currentIndex\t\t" + currentIndex);
			int nextIndex = normalizeCyclicI(currentIndex - 1, bits.length);
			System.out.println("nextIndex\t\t" + nextIndex);

//			if(currentIndex==bits.length-1) {
//				break;
//			}

			long currentIndexValue = bits[currentIndex];
			System.out.println("currentValue\t\t" + BinaryStrings.toBinaryString(currentIndexValue));
			long newCurrentIndexValue = currentIndexValue << (unit.bits() - lastIndexBits);
			System.out.println("leftShift: " + (currentIndexValue << (unit.bits() - lastIndexBits)));
			System.out.println("newCurrentIndexValue\t" + BinaryStrings.toBinaryString(newCurrentIndexValue));

			bits[currentIndex] = newCurrentIndexValue;
			long orVal = bits[nextIndex];
			System.out.println("orVal\t\t\t" + BinaryStrings.toBinaryString(orVal));
			orVal &= prevIdxGrabMask;
			System.out.println("orVal&prevIdGrabMask\t" + BinaryStrings.toBinaryString(orVal));
			orVal = orVal >>> lastIndexBits;
			System.out.println("orVal>>>lastIndexBits\t" + BinaryStrings.toBinaryString(orVal));
			System.out.println("bits[currentIndex]\t" + BinaryStrings.toBinaryString(bits[currentIndex]));
			bits[currentIndex] |= orVal;
			System.out.println("bits[currentIndex]\t" + BinaryStrings.toBinaryString(bits[currentIndex]));
//			if (nextIndex != shiftedLastSegmentIndex) {
			long nextIndexBits = bits[nextIndex];
			System.out.println("nextIndexBits\t\t" + BinaryStrings.toBinaryString(nextIndexBits));
			bits[nextIndex] &= ~prevIdxGrabMask;
			nextIndexBits = bits[nextIndex];
			System.out.println("nextIndexBits\t\t" + BinaryStrings.toBinaryString(nextIndexBits));
//			}
			System.out.println("===========END========");

		}

	}

	public static void main(String[] args) {
		int bitLen = 128 + 3;
		LongQuickBitArray lqba = new LongQuickBitArray(bitLen);// LongQuickBitArray.createRandomArrayOfLength(bitLen);

//		LongQuickBitArray lqba2 = lqba.clone();
//		lqba.set(64);
//		bitwiseRotate(lqba2.getBitArray(), lqba2.getSize(), 0);
//		System.out.println(lqba);
//		System.out.println(lqba2);
//		System.out.println(BinaryStrings.toBinaryString(lqba2.getBitArray(),lqba2.getSize(),""));
//		System.out.println(new LongQuickBitArray(bs, bitLen));
//		

//		for(int i=0;i<bs.length;i++) {
//			bs[i]=Long.reverse(bs[i]);
//		}
//		String asStr=BinaryStrings.toBinaryString(bs,lqba.getSize(),"").replace('1','█').replace('0', '_');
//		asStr=asStr.substring(1,asStr.length()-1);
//		StringBuilder sb = new StringBuilder();
//		for(int i=0;i<asStr.length();i++) {
//			if((i&7)==0) {
//				sb.append(' ');
//			}
//			sb.append(asStr.charAt(i));
//		}
//		System.out.println(sb);
//		int start=1;
//		int end=68;
//		long[] bits=lqba.getBitArray();
//		printSubArrayComparison(bits, bitLen, start, end);
//		

//		System.out.println(-1+":\t"+BinaryStrings.toBinaryString(lqba.getBitArray(),lqba.getSize()));
//		System.out.println("-1:\t"+BinaryStrings.toBinaryString(lqba.getBitArray(),lqba.getSize()));
		int rotations = (bitLen * 4) + 12;
		lqba.set(0);
		lqba.set(61);
		lqba.set(62);
		lqba.set(63);
		lqba.set(64);
		lqba.set(67);
		lqba.set(128);
		System.out.println(lqba);
		System.out.println(BinaryStrings.toBinaryString(lqba.getBitArray(), lqba.getSize()));
		LongQuickBitArray lqba2 = lqba.clone();
		shiftLeft(lqba.getBitArray(), lqba.getSize(), 1);
		System.out.println(lqba);
		shiftLeft(lqba.getBitArray(), lqba.getSize(), 1);
		System.out.println(lqba);
		shiftLeft(lqba.getBitArray(), lqba.getSize(), 1);
		System.out.println(lqba);
		shiftLeft(lqba.getBitArray(), lqba.getSize(), -1);
		System.out.println(lqba);
		shiftLeft(lqba.getBitArray(), lqba.getSize(), -1);
		System.out.println(lqba);
		shiftLeft(lqba.getBitArray(), lqba.getSize(), -1);
		System.out.println(lqba);
//		getOrValsForLeftShift(lqba.getBitArray(), 63, lqba.getSize());
//		LongQuickBitArray lbqa2 = lqba.clone();
//		for (int i = 0; i < 32; i++) {
//			bitwiseRotate(lqba.getBitArray(), lqba.getSize(), 1);
//			lbqa2.shift(64*(i+1));
//			System.out.println("  " + BinaryStrings.toBinaryString(lqba.getBitArray(), lqba.getSize()));
//			System.out.println("  " + lqba);
//			System.out.println("2 " + BinaryStrings.toBinaryString(lbqa2.getBitArray(), lbqa2.getSize()));
//			System.out.println("2 " + lbqa2);
//
//		}
////		for (int i = 0; i < lqba.getBitArray().length; i++) {
////			shiftUnitsRightAndRejoin(lqba.getBitArray(), lqba.getSize(), 1);
////
////			System.out.println(BinaryStrings.toBinaryString(lqba.getBitArray(), lqba.getSize()));
////			System.out.println(lqba);
//////			lqba.unset(i);
//////			System.out.println(i+":\t"+BinaryStrings.toBinaryString(nonMutatingBitwiseRotate(lqba.getBitArray(), lqba.getSize(), i),lqba.getSize()));
//		}

	}
}
