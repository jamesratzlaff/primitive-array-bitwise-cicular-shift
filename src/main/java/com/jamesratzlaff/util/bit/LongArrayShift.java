package com.jamesratzlaff.util.bit;

import java.util.stream.IntStream;

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
		long[] copy = bits.clone();
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
		return shiftRight(bits, (int) size, (int) amt);
	}
	public static long[] nonMutatingBitwiseRotateInnerBits(long[] bits, long size, long amt, int innerOffset, int innerEndOffsetExcl) {
		long[] clone=bits.clone();
		return bitwiseRotateInnerBits(clone, size, amt, innerOffset, innerEndOffsetExcl);
	}
	public static long[] bitwiseRotateInnerBits(long[] bits, long size, long amt, int innerOffset, int innerEndOffsetExcl) {
		long[] subBits = subBits(bits, innerOffset, innerEndOffsetExcl);
		long[] XORSubbits = offset(subBits, innerOffset);
		bitwiseRotate(subBits, innerEndOffsetExcl-innerOffset, amt);
		subBits=offset(subBits, innerOffset);
		nonCyclicXor(bits, XORSubbits);
		nonCyclicOr(bits, subBits);
		return bits;
	}
	private static void nonCyclicOr(long[] toOr, long[] orer) {
		int endIdx=Math.min(toOr.length, orer.length);
		for(int i=0;i<endIdx;i++) {
			toOr[i]|=orer[i];
		}
	}
	private static void nonCyclicXor(long[] toXor, long[] xorer) {
		int endIdx=Math.min(toXor.length, xorer.length);
		for(int i=0;i<endIdx;i++) {
			toXor[i]^=xorer[i];
		}
	}

	
	public static long[] bitwiseRotateInlined(long[] bits, long size, long amt) {
		return shiftRightInlined(bits, (int) size, (int) amt);
	}

	public static long[] bitwiseRotateUsingSubArrays(long[] bits, long size, long amt) {
		amt=normalizeCyclic(amt, size);
		long[] endOfResult =  offset(subBits(bits, 0, (int)amt),(int)(size-amt));
		long[] beginningOfResult = subBits(bits, (int)amt, (int)size);
		nonCyclicOr(endOfResult, beginningOfResult);
		return endOfResult;
	}

	/**
	 * rotates the integral representation of bits by the amount of given shift
	 * units and fills in the empty bits from what was the last integral
	 * representation with the values preceeding it and natrually trims the new last
	 * intregral bit representation to the correct size
	 * 
	 * <pre>
	 * integral_unit_size = 8 (bits)
	 * (with a bit length of 17)
	 * bitno:        7  6  5  4  3  2  1 0, 15 14 13 12 11 10 9 8,                      16
	 *              {1  0  0  1  1  0  0 1, 1  1  1  0  1  0  1 0, 0  0  0  0  0  0  0  1}
	 * integral idx:                     0                      1                       2
	 * (rotate 2)
	 * prev integral idx:                1                      2                      0 
	 *              {1  0  0  1  1  0  0 1, 0  0  0  0  0  0  0 1, 1  1  1  0  1  0  1 0}
	 * new integral idx:                 0                      1                      2
	 * (collapse[mask->shift->or[prevIdx])
	 * lastIndexBits=17%integral_unit_size=1
	 * lastIndexCarryMask=-1<<lastIndexBits (11111110)
	 * using new integral indices: 1 carries from 0, 2 carries from 1
	 * we start from the index that represents the integral that was originally the last integral
	 * representation before rotation (in this case 1)
	 * -we shift it's value left by integral_unit_size-lastIndexBits (7 in this case)
	 * -we take that value and or it (with the previous indexes value &'d with the last
	 *  index carry mask which is then unsigned shifted right by lastIndexBits value)
	 * -we then & and assign the previous indexes value with the inverse of the 
	 *   lastIndexCarryMask (00000001)
	 * -and we iterate backward to the previous value and repeat (like above or using the
	 *  right-most index as the previous index if the current index is 0 
	 *              {1  0  0  1  1  0  0 1, 0  0  0  0  0  0  0 1, 1  1  1  0  1  0  1 0}
	 *                                      << 7
	 *                                      1  0  0  0  0  0  0 0
	 *              &1  1  1  1  1  1  1 0                       
	 *               1  0  0  1  1  0  0 0,
	 *               >> 1
	 *               0  1  0  0  1  1  0 0
	 *                                     |
	 *                                      1  1  0  0  1  1  0 0
	 *            &~(1  1  1  1  1  1  1 0)
	 *               0  0  0  0  0  0  0 1                              
	 *              {0  0  0  0  0  0  0 1, 1  1  0  0  1  1  0 0, 1  1  1  0  1  0  1 0}
	 *            << 7
	 *               1  0  0  0  0  0  0 0
	 *                                                            &1  1  1  1  1  1  1 0
	 *                                                             1  1  1  0  1  0  1 0
	 *                                                           >>1
	 *                                                             0  1  1  1  0  1  0 1
	 *                                              | 
	 *               1  1  1  1  0  1  0 1
	 *                                                          &~(1  1  1  1  1  1  1 0)
	 *                                                             0  0  0  0  0  0  0 0
	 *              {1  1  1  1  0  1  0 1, 1  1  0  0  1  1  0 0, 0  0  0  0  0  0  0 0}
	 * 
	 * </pre>
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

	private static long[] rotateAndCollapseForLeftShift(long[] bits, int size, int shiftUnits) {
		rotate(bits, -shiftUnits);
		int lastIndex = bits.length - 1;
		int newLastBitsIndex = normalizeCyclicI(lastIndex - shiftUnits, bits.length);
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

	/**
	 * This actually operates more like getting the carries for left shifting btw
	 * 
	 * @param bits
	 * @param size
	 * @param amt
	 * @return
	 */
	private static long[] getOrValsForRightShift(long[] bits, int size, int amt) {
		if (amt == 0) {
			return new long[bits.length];
		}
		int lastIndex = bits.length - 1;
		int numberOfLastIndexBits = size & unit.limitMask();
		if (size != 0 && numberOfLastIndexBits == 0) {
			numberOfLastIndexBits = unit.bits();
		}
		long carryMask = ~(-1l >>> amt);
		long lastIndexCarryMask = amt >= numberOfLastIndexBits ? ~(-1l << numberOfLastIndexBits)
				: -1l << (numberOfLastIndexBits - amt);
		long bleedOverMask = amt >= numberOfLastIndexBits ? ~(-1l >>> (amt - numberOfLastIndexBits)) : 0;
		int numberOfShifts = unit.bits() - amt;
		int lastIndexCarryShifts = unit.bits() - numberOfLastIndexBits;

		long[] orVals = new long[bits.length];

		for (int i = 0; i < bits.length; i++) {
			int currentIndex = i;
			long currentVal = bits[currentIndex];
			if (currentIndex == lastIndex) {//removing this if branch increases performance by 10%...consider it
				long orVal = currentVal&lastIndexCarryMask;
				orVal <<= lastIndexCarryShifts;
				if (currentIndex != 0 && bleedOverMask != 0) {
					long bleedOver = orVals[currentIndex - 1];
					bleedOver <<= numberOfShifts;
					long bleedOverVal = bleedOverMask & bleedOver;
					bleedOverVal >>>= numberOfLastIndexBits;
					orVal |= bleedOverVal;
					bleedOver &= ~bleedOverMask;
					bleedOver >>>= numberOfShifts;
					orVals[currentIndex - 1] = bleedOver;
				}
				orVal >>>= numberOfShifts;
				orVals[currentIndex] = orVal;
			} else {
				long orVal = currentVal & carryMask;
				orVal >>>= numberOfShifts;
				orVals[currentIndex] = orVal;
			}
		}
		return orVals;

	}
	
	
	
	

	/**
	 * only slightly (2ns/op) faster than non inlined
	 * @param bits
	 * @param size
	 * @param amt
	 * @return
	 */
	private static long[] getOrValsForRightShiftInlined(long[] bits, int size, int amt) {
		if (amt == 0) {
			return new long[bits.length];
		}
		int lastIndex = bits.length - 1;
		int numberOfLastIndexBits = size & unit.limitMask();
		if (size != 0 && numberOfLastIndexBits == 0) {
			numberOfLastIndexBits = unit.bits();
		}
		long carryMask = ~(-1l >>> amt);
		long lastIndexCarryMask = amt >= numberOfLastIndexBits ? ~(-1l << numberOfLastIndexBits)
				: -1l << (numberOfLastIndexBits - amt);
		long bleedOverMask = amt >= numberOfLastIndexBits ? ~(-1l >>> (amt - numberOfLastIndexBits)) : 0;
		int numberOfShifts = unit.bits() - amt;
		int lastIndexCarryShifts = unit.bits() - numberOfLastIndexBits;

		long[] orVals = new long[bits.length];

		for (int i = 0; i < bits.length; i++) {
			int currentIndex = i;
			if (currentIndex == lastIndex) {
				long orVal = (bits[currentIndex] & lastIndexCarryMask) << lastIndexCarryShifts;
				if (currentIndex != 0 && bleedOverMask != 0) {
					long bleedOver = orVals[currentIndex - 1]<<numberOfShifts;
					long bleedOverVal = (bleedOverMask & bleedOver)>>>numberOfLastIndexBits;
					orVal |= bleedOverVal;
					bleedOver = (bleedOver&(~bleedOverMask))>>>numberOfShifts;
					orVals[currentIndex - 1] = bleedOver;
				}
				orVals[i] =(orVal >>>numberOfShifts);
			} else {
				long currentValue = bits[currentIndex];
				orVals[i] = (currentValue & carryMask)>>>numberOfShifts;
			}
		}
		return orVals;

	}

	

	private static long[] shiftRight(long[] bits, int size, int amt) {
		amt = normalizeCyclicI(amt, size);
		int unitShifts = amt >>> unit.multOrDivShift();
		amt -= (unitShifts << unit.multOrDivShift());
		unitShifts = unitShifts % bits.length;
		int numberOfLastBits = size & unit.limitMask();

		long lastBitsMask = -1l >>> unit.bits() - numberOfLastBits;

		if (unitShifts != 0) {
			rotateAndCollapseForRightShift(bits, size, unitShifts);
		}
		long[] orVals = getOrValsForRightShift(bits, size, amt);
		rotate(orVals, 1);

		for (int i = 0; i < bits.length; i++) {
			bits[i] = ((bits[i] << amt) | orVals[i]);
		}
		bits[bits.length - 1] &= lastBitsMask;
		return bits;
	}

	private static long[] shiftRightInlined(long[] bits, int size, int amt) {
		amt = normalizeCyclicI(amt, size);
		int unitShifts = amt >>> unit.multOrDivShift();
		amt -= (unitShifts << unit.multOrDivShift());
		unitShifts = unitShifts % bits.length;
		int numberOfLastBits = size & unit.limitMask();

		long lastBitsMask = -1l >>> unit.bits() - numberOfLastBits;

		if (unitShifts != 0) {
			rotateAndCollapseForRightShift(bits, size, unitShifts);
		}
		long[] orVals = getOrValsForRightShiftInlined(bits, size, amt);
		rotate(orVals, 1);

		for (int i = 0; i < bits.length; i++) {
			bits[i] = ((bits[i] << amt) | orVals[i]);
		}
		bits[bits.length - 1] &= lastBitsMask;
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

	public static int normalizeCyclicI(long value, long bound) {
		return (int) normalizeCyclic(value, bound);
	}

	public static long normalizeCyclic(long value, long bound) {
		if (value < 0) {
			if (-value > bound) {
				value = quickMod(value, bound);
			}
			value = bound + value;
		}
		if (value == bound) {
			return 0;
		}
		if (value > bound) {
			value = quickMod(value, bound);
		}

		return value;
	}

	private static long quickMod(long value, long bound) {
		if (isPowerOf2(bound)) {
			return value & (bound - 1);
		}
		return value % bound;
	}

	private static boolean isPowerOf2(long amt) {
		if (amt < 0) {
			amt = -amt;
		}
		return Long.bitCount(amt) == 1;
	}

	public static long[] getOffsetSubBits(long[] bits, int startBit, int endExclusive) {
		long[] subbits = subBits(bits, startBit, endExclusive);
		subbits = offset(subbits, startBit);
		return subbits;
	}

	public static long[] subBits(long[] bits, int startBit, int endExclusive) {
		int merge = startBit ^ endExclusive;
		startBit = Math.min(merge ^ startBit, merge ^ endExclusive);
		endExclusive = merge ^ startBit;
		return getValues(bits, startBit, endExclusive);
	}

	private static long[] getValues(long[] longs, int offset, int endOffset) {
		int maxOffset = longs.length << unit.multOrDivShift();
		endOffset = Math.min(maxOffset, endOffset);
		int totalBits = endOffset - offset;
		int arrSize = (totalBits >>> unit.multOrDivShift()) + ((totalBits & unit.limitMask()) > 0 ? 1 : 0);
		arrSize = Math.min(longs.length, arrSize);

		long[] vals = new long[arrSize];
		for (int i = 0; i < arrSize; i++) {
			int offsetToUse = offset + (i << unit.multOrDivShift());
			vals[i] = getValue(longs, offsetToUse);
		}
		int lastIndexSize = totalBits & unit.limitMask();
		if (lastIndexSize != 0 || lastIndexSize != unit.bits()) {
			long lastIndexMask = -1l >>> (unit.bits() - lastIndexSize);
			vals[vals.length - 1] &= lastIndexMask;
		}
		return vals;
	}

	/**
	 * 
	 * @param longs
	 * @param amount the amount of '0' bits that should be prepended to the first
	 *               value in this array
	 * @return a copy of <code>longs</code> with padding (should not modify passed in instance)
	 */
	private static long[] offset(long[] longs, int amount) {
		int numberOfZerosInLastElement = Long.numberOfLeadingZeros(longs[longs.length - 1]);
		int wholeUnits = amount >> unit.multOrDivShift();
		amount -= wholeUnits << unit.multOrDivShift();
		int amountAndZeroDiff = amount - numberOfZerosInLastElement;
		int expand = amountAndZeroDiff > 0 ? 1 : 0;
		int orValSize = (longs.length - 1) + expand;
		long[] orVals = new long[orValSize];
		long orMask = ~(-1l >>> amount);
		
		int shiftAmt=unit.bits()-amount;
		
		long[] result = new long[longs.length+wholeUnits+expand];
		System.arraycopy(longs, 0, result, wholeUnits, longs.length);
		
//		longs.clone();
//		if (orValSize == longs.length) {
//			result = new long[orValSize + 1];
//			System.arraycopy(longs, 0, result, 0, longs.length);
//		}

		for (int i = 0; i < orValSize; i++) {
			int shift = shiftAmt;
			long current = longs[i];
			long orVal = current & orMask;
			orVal >>>= shift;
			orVals[i] = orVal;
		}

		for (int i = 0; i < longs.length; i++) {
			int shift = amount;
			long orVal = 0;
			int orValIdx = i - 1;
			if (orValIdx > -1 && orValIdx < orVals.length) {
				orVal = orVals[orValIdx];
			}
			int resultIdx=i+wholeUnits;
			result[resultIdx] <<= shift;
			result[resultIdx] |= orVal;
		}
		if (expand==1) {
			result[result.length - 1] = orVals[orVals.length - 1];
		}
		return result;
	}

	private static long getValue(long[] longs, int offset) {
		int idx = offset >>> unit.multOrDivShift();
		int endIdx = (offset + unit.bits()) >>> unit.multOrDivShift();
		int endOffset = (offset + unit.bits()) & unit.limitMask();
		if (idx == endIdx && endOffset == 0) {
			endOffset = unit.bits() - 1;
		}
		long lowerMaskSize = offset & unit.limitMask();
		if (lowerMaskSize == 0) {
			lowerMaskSize = unit.bits();
		}

		long lowerMask = (-1l << lowerMaskSize);
		long upperMask = -1l >>> (unit.bits() - endOffset);// BinaryStrings.toBinaryString(lowerMask,upperMask)
		long value = (longs[idx] & lowerMask) >>> (lowerMaskSize);
		if (Long.numberOfTrailingZeros(lowerMask) != 0 && endIdx < longs.length) {
			value |= (longs[endIdx] & upperMask) << (unit.bits() - endOffset);
		}
		return value;
	}

	public static void main(String[] args) {
		int bitLen = 128 + 19;
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
		rando.set(148);
		LongQuickBitArray randoClone = rando.clone();
		
//		System.out.println(BinaryStrings.toBinaryString(rando.getBitArray()));
//		long[] sub = getValues(rando.getBitArray(), 3, 149);
//		System.out.println(BinaryStrings.toBinaryString(sub));
//		System.out.println(BinaryStrings.toBinaryString(offset(sub, 63)));
//		System.out.println(rando);
//
//		System.out.println(rando);
//		int expandOffset=65;
//		int expandEnd=65+65;
//		bitwiseRotateInnerBits(rando.getBitArray(), rando.getSize(), 1, expandOffset, expandEnd);
//		System.out.println(rando);
//		System.out.println(BinaryStrings.toBinaryString(subs));

	}
}
