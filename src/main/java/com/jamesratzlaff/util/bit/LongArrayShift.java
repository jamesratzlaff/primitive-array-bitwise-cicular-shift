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
	 * @param bits an <code>long[]</code> that represents bits
	 * @param size the number of <i>bit</i> this <code>int[]</code> represents
	 * @param amt  the amount to rotate the bits, a positive value will rotate
	 *             right, a negative value will rotate left
	 * @return the passed in <code>bits</code> parameter with its bits rotated
	 */
	public static long[] bitwiseRotate(long[] bits, long size, long amt) {
		return shiftRight(bits, (int)size, (int)amt);
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
	private static long[] rotateAndCollapseForRightShift(long[] bits, int size, int shiftUnits) {
		rotate(bits, shiftUnits);
		int lastIndex = bits.length - 1;
		int newLastBitsIndex = normalizeCyclicI(lastIndex + shiftUnits, bits.length);
		int numberOfLastIndexBits = size & unit.limitMask();
		if (numberOfLastIndexBits == 0) {
			return bits;
		}
		long lastIndexCarryMask = -1l << numberOfLastIndexBits;
		int numberOfShifts = unit.bits() - numberOfLastIndexBits;
		for (int i = 0; i < bits.length; i++) {
			int currentIndex = normalizeCyclicI(newLastBitsIndex - i, bits.length);
			int prevIndex = normalizeCyclicI(currentIndex - 1, bits.length);
			if (currentIndex == lastIndex) {
				break;
			}
			long currentValue = bits[currentIndex];
			currentValue <<= numberOfShifts;
			long prevValue = bits[prevIndex];
			long orVal = prevValue & lastIndexCarryMask;
			orVal >>>= numberOfLastIndexBits;
			long newPrevValueMask = ~(-1l << numberOfLastIndexBits);
			prevValue &= newPrevValueMask;
			currentValue |= orVal;
			bits[currentIndex] = currentValue;
			bits[prevIndex] = prevValue;
		}
		return bits;

	}

	private static long[] getOrValsForRightShift(long[] bits, int size, int amt) {
		int lastIndex = bits.length - 1;
		int numberOfLastIndexBits = size & unit.limitMask();
		if(size!=0&&numberOfLastIndexBits==0) {
			numberOfLastIndexBits=unit.bits();
		}
		long carryMask = ~(-1l >>> amt);
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
						long bleedOverVal=bleedOverMask&bleedOver;
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
	
	private static long[] shiftRight(long[] bits, int size, int amt) {
		
		amt = normalizeCyclicI(amt, size);
		int unitShifts = amt >>> unit.multOrDivShift();
		amt -= (unitShifts << unit.multOrDivShift());
		int numberOfLastBits = size&unit.limitMask();
		
		long lastBitsMask = -1l>>>unit.bits()-numberOfLastBits;
		
		if(unitShifts>0) {
			rotateAndCollapseForRightShift(bits, size, unitShifts);
		}
		long[] orVals = getOrValsForRightShift(bits, size, amt);
		rotate(orVals, 1);
		for(int i=0;i<bits.length;i++) {
			bits[i]=((bits[i]<<amt)|orVals[i]);
		}
		bits[bits.length-1]&=lastBitsMask;
		return bits;
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
	//TODO: implement non-cyclic shifting :-(
	public static long[] subBits(long[] bits, int startBit, int endExclusive) {
		int merge = startBit ^ endExclusive;
		startBit = Math.min(merge ^ startBit, merge ^ endExclusive);
		endExclusive = merge ^ startBit;
		int startIdx = getArrayIdxUsingBitIdx(startBit);
		int endIdx = getArrayIdxUsingBitIdx(endExclusive);
		int len = (endIdx - startIdx) + 1;
		long[] reso = new long[len];
		System.arraycopy(bits, startIdx, reso, 0, len);
		System.out.println(BinaryStrings.toBinaryString(reso));
		int reducedSize = endExclusive - startBit;
		endExclusive = reducedSize & unit.limitMask();
		startBit = startBit & unit.limitMask();
		long endMask = -1l>>>unit.bits()-endExclusive;
//		reso[0]&=~(1<<startBit);
//		reso[len - 1] &= endMask;
		bitwiseRotate(reso, reso.length<<unit.multOrDivShift(), -startBit);
		//TODO: snip off the right-most bits
		
		
		System.out.println(BinaryStrings.toBinaryString(reso));
		System.out.println(startBit);
//		reso[len-1]<<=startBit;
//		bitwiseRotate(reso,reso.length<<unit.multOrDivShift(),startBit);
//		reso[len - 1] &= endMask;
//		reso[len-1]<<=startBit;
		System.out.println(BinaryStrings.toBinaryString(reso));
		
		
		
		System.out.println(BinaryStrings.toBinaryString(reso));
		return reso;
	}
	


	private static void shiftLeft(long[] bits, int amt) {
		bitwiseRotate(bits, bits.length<<unit.multOrDivShift(), amt);
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
		LongQuickBitArray rando = LongQuickBitArray.createRandomArrayOfLength(bitLen);
		System.out.println(rando);
		rando.set(62);
		rando.set(63);
		rando.set(64);
		rando.set(65);
		rando.set(126);
		rando.set(127);
		rando.set(128);
		rando.set(129);
		rando.set(130);
		long[] subs = subBits(rando.getBitArray(), 1, 68);
		LongQuickBitArray subArray = new LongQuickBitArray(subs, 67);
		System.out.println(subArray);
		System.out.println(BinaryStrings.toBinaryString(subs));


	}
}
