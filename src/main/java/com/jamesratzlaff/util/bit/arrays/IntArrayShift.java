package com.jamesratzlaff.util.bit.arrays;

import com.jamesratzlaff.util.bit.BinaryStrings;
import com.jamesratzlaff.util.bit.BitUnit;

public class IntArrayShift {

	private static final BitUnit unit = BitUnit.INT;

	/**
	 * 
	 * @param bits an <code>long[]</code> that represents bits
	 * @param size the number of <i>bit</i> this <code>long[]</code> represents
	 * @param amt  the amount to rotate the bits, a positive value will rotate
	 *             right, a negative value will rotate left
	 * @return a copy of the passed in <code>bits</code> parameter with the copy's
	 *         bits rotated
	 */
	public static int[] nonMutatingBitwiseRotate(int[] bits, int size, int amt) {
		int[] copy = bits.clone();
		return bitwiseRotate(copy, size, amt);
	}

	/**
	 * for some reason negative amounts take ~2.5 longer than postitive amounts
	 * 
	 * @param bits an <code>long[]</code> that represents bits
	 * @param size the number of <i>bit</i> this <code>int[]</code> represents
	 * @param amt  the amount to rotate the bits, a positive value will rotate
	 *             right, a negative value will rotate left
	 * @return the passed in <code>bits</code> parameter with its bits rotated
	 */
	public static int[] bitwiseRotate(int[] bits, int size, int amt) {
		if(amt>=0) {
		return shiftRight(bits, (int) size, (int) amt);
		}
		return bitwiseRotateUsingSubArrays(bits, size, amt);
	}

	

	public static int[] nonMutatingBitwiseRotateInnerBits(int[] bits, int size, int amt, int innerOffset,
			int innerEndOffsetExcl) {
		int[] clone = bits.clone();
		return bitwiseRotateInnerBits(clone, size, amt, innerOffset, innerEndOffsetExcl);
	}

	public static int[] bitwiseRotateInnerBits(int[] bits, int size, int amt, int innerOffset,
			int innerEndOffsetExcl) {
		int[] subBits = subBits(bits, innerOffset, innerEndOffsetExcl);
		int[] XORSubbits = offset(subBits, innerOffset);
		bitwiseRotate(subBits, innerEndOffsetExcl - innerOffset, amt);
		subBits = offset(subBits, innerOffset);
		nonCyclicXor(bits, XORSubbits);
		nonCyclicOr(bits, subBits);
		return bits;
	}

	private static void nonCyclicOr(int[] toOr, int[] orer) {
		int endIdx = Math.min(toOr.length, orer.length);
		for (int i = 0; i < endIdx; i++) {
			toOr[i] |= orer[i];
		}
	}

	private static void nonCyclicXor(int[] toXor, int[] xorer) {
		int endIdx = Math.min(toXor.length, xorer.length);
		for (int i = 0; i < endIdx; i++) {
			toXor[i] ^= xorer[i];
		}
	}

	

	/**
	 * For some reason this is much faster with negative amounts than positive
	 * amounts. This is also faster with negative amounts compared to
	 * {{@link #bitwiseRotate(long[], long, long)}
	 * 
	 * @param bits
	 * @param size
	 * @param amt
	 * @return
	 */
	public static int[] bitwiseRotateUsingSubArrays(int[] bits, int size, int amt) {
		amt = -amt;
		amt = normalizeCyclic(amt, size);
		if (amt == 0) {
			return bits;
		}
		int[] endOfResultSubBits = subBits(bits, 0, (int) amt);
		int[] endOfResult = offset(endOfResultSubBits, (int) (size - amt));
		int[] beginningOfResult = subBits(bits, (int) amt, (int) size);
		nonCyclicOr(endOfResult, beginningOfResult);
		System.arraycopy(endOfResult, 0, bits, 0, endOfResult.length);
		return bits;
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
	private static int[] rotateAndCollapseForRightShift(int[] bits, int size, int shiftUnits) {
		rotate(bits, shiftUnits);
		int lastIndex = bits.length - 1;
		int newLastBitsIndex = normalizeCyclicI(lastIndex + shiftUnits, bits.length);
		int numberOfLastIndexBits = size & unit.limitMask();

		if (numberOfLastIndexBits == 0) {
			return bits;
		}
		int lastIndexCarryMask = -1 << numberOfLastIndexBits;
		int numberOfShifts = unit.bits() - numberOfLastIndexBits;
		for (int i = 0; i < bits.length; i++) {
			int currentIndex = normalizeCyclicI(newLastBitsIndex - i, bits.length);
			int prevIndex = normalizeCyclicI(currentIndex - 1, bits.length);
			if (currentIndex == lastIndex) {
				break;
			}
			int currentValue = bits[currentIndex];
			currentValue <<= numberOfShifts;
			int prevValue = bits[prevIndex];
			int orVal = prevValue & lastIndexCarryMask;
			orVal >>>= numberOfLastIndexBits;
			long newPrevValueMask = ~(-1l << numberOfLastIndexBits);
			prevValue &= newPrevValueMask;
			currentValue |= orVal;
			bits[currentIndex] = currentValue;
			bits[prevIndex] = prevValue;
		}
		return bits;

	}

	


	private static long aEQb(long a, long b) {
		switch ((int) (a ^ b)) {
		case 0:
			return 1;
		default:
			return 0;
		}
	}

	 static long min(long a, long b) {
		return b ^ ((a ^ b) & -(((a - b) & ~(-1l >>> 1)) >>> unit.limitMask()));
	}

	 static long max(long a, long b) {
		return a ^ ((a ^ b) & -(((a - b) & ~(-1l >>> 1)) >>> unit.limitMask()));
	}

	 static int min(int a, int b) {
		return b ^ ((a ^ b) & -(((a - b) & ~(-1 >>> 1)) >>> BitUnit.INT.limitMask()));
	}

	 static int max(int a, int b) {
		return a ^ ((a ^ b) & -(((a - b) & ~(-1 >>> 1)) >>> BitUnit.INT.limitMask()));
	}

	 static long aEQbI(long a, long b) {
		return (int) aEQb(a, b);
	}

	 static long aLTb(long a, long b) {
		// ((a-b)&~(-1l>>>1))>>>unit.limitMask()
		return ((a - b) & ~(-1l >>> 1)) >>> unit.limitMask();
	}

	 static int aLTb(int a, int b) {
		// ((a-b)&~(-1l>>>1))>>>unit.limitMask()
		return ((a - b) & ~(-1 >>> 1)) >>> BitUnit.INT.limitMask();
	}

	 static int aLTbI(long a, long b) {
		return (int) aLTb(a, b);
	}

	 static long aGTb(long a, long b) {
		switch ((int) ((((a >>> (unit.bits() >> 1)) ^ (b >>> (unit.bits() >> 1)))
				| ((a & (-1l >>> (unit.bits() >> 1))) ^ (b & (-1l >>> (unit.bits() >> 1))))))) {
		case 0:
			return 0;
		default:
			return (~((~(((a - b) & ~(-1l >>> 1)) >>> unit.limitMask()))) - 1) & 1;
		}
//		return (~((~(aEQb(a, b))) & (~(((a-b)&~(-1l>>>1))>>>unit.limitMask())))-1)&1;
	}

	/**
	 * This actually operates more like getting the carries for left shifting btw
	 * 
	 * @param bits
	 * @param size
	 * @param amt
	 * @return
	 */
	public static int[] getOrValsForRightShift(int[] bits, int size, int amt) {
		switch (amt) {
		case 0:
			return new int[bits.length];
		default: {
			int lastIndex = bits.length - 1;
			int numberOfLastIndexBits = size & unit.limitMask();
			switch (size) {
			case 0:
				break;
			default:
				switch (numberOfLastIndexBits) {
				case 0:
					numberOfLastIndexBits = unit.bits();
				default:
					break;
				}
			}
			int carryMask = ~(-1 >>> amt);
			int lastIndexCarryMask = -1 << (numberOfLastIndexBits - amt);
			int bleedOverMask = 0;
			switch (aLTb(numberOfLastIndexBits, amt)) {
			case 1:
				lastIndexCarryMask = ~(-1 << numberOfLastIndexBits);
				bleedOverMask = ~(-1 >>> (amt - numberOfLastIndexBits));
				break;
			default:
				break;
			}
			int numberOfShifts = unit.bits() - amt;
			int lastIndexCarryShifts = unit.bits() - numberOfLastIndexBits;

			int[] orVals = new int[bits.length];

			for (int i = 0; i < lastIndex; i++) {
				int currentIndex = i;
				int currentVal = bits[currentIndex];
				int orVal = currentVal & carryMask;
				orVal >>>= numberOfShifts;
				orVals[currentIndex] = orVal;
			}
			int currentVal = bits[lastIndex];
			int orVal = currentVal & lastIndexCarryMask;
			orVal <<= lastIndexCarryShifts;
			switch (lastIndex) {

			case 0:
				break;
			default: {
				switch (Long.bitCount(bleedOverMask)) {
				case 0:
					break;
				default: {
					int prevIndex = lastIndex - 1;
					int bleedOver = orVals[prevIndex];
					bleedOver <<= numberOfShifts;
					int bleedOverVal = bleedOverMask & bleedOver;
					bleedOverVal >>>= numberOfLastIndexBits;
					orVal |= bleedOverVal;
					bleedOver &= ~bleedOverMask;
					bleedOver >>>= numberOfShifts;
					orVals[prevIndex] = bleedOver;
				}
				}
			}
			}
			orVal >>>= numberOfShifts;
			orVals[lastIndex] = orVal;
			return orVals;
		}
		}

	}



	private static int[] shiftRight(int[] bits, int size, int amt) {
		amt = normalizeCyclicI(amt, size);
		int unitShifts = amt >>> unit.multOrDivShift();
		amt -= (unitShifts << unit.multOrDivShift());
		unitShifts = (int) quickMod(unitShifts, bits.length);
		int numberOfLastBits = size & unit.limitMask();

		int lastBitsMask = -1 >>> (unit.bits() - numberOfLastBits);
		switch (unitShifts) {
		case 0:
			break;
		default:
			rotateAndCollapseForRightShift(bits, size, unitShifts);
		}

		int[] orVals = getOrValsForRightShift(bits, size, amt);
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
	public static int[] rotate(int[] array, int amt) {
		int[] reso = nonMutatingRotate(array, amt);
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
	public static int[] nonMutatingRotate(int[] array, int amt) {
		amt = (int) normalizeCyclic(amt, array.length);
		if (amt > 0) {
			int[] result = new int[array.length];
			int toIdx = array.length - amt;
			System.arraycopy(array, toIdx, result, 0, amt);
			System.arraycopy(array, 0, result, amt, toIdx);
			return result;
		}
		return array;
	}

	public static int normalizeCyclicI(int value, int bound) {
		return (int) normalizeCyclic(value, bound);
	}

	public static int normalizeCyclic(int value, int bound) {
		int valComp = -(int) aLTb(value, 0);
		switch (valComp) {
		case 0:
			valComp = (int) aGTb(value, bound);
		}
		switch (valComp) {
		case -1: {
			switch ((int) aGTb(-value, bound)) {
			case 1:
				value = quickMod(value, bound);
				break;
			}
			value = bound + value;
			break;
		}
		}
		if (value == bound) {
			return 0;
		}
		if (value > bound) {
			value = quickMod(value, bound);
		}

		return value;

	}

	private static int quickMod(int value, int bound) {
		if (isPowerOf2(bound)) {
			return value & (bound - 1);
		}
		return value % bound;
	}

	private static boolean isPowerOf2(long amt) {
		switch((int)aLTb(amt,0)) {
		case 1:
			amt=-amt;
			break;
		}
		return Long.bitCount(amt) == 1;
	}

	public static int[] getOffsetSubBits(int[] bits, int startBit, int endExclusive) {
		int[] subbits = subBits(bits, startBit, endExclusive);
		subbits = offset(subbits, startBit);
		return subbits;
	}

	public static int[] subBits(int[] bits, int startBit, int endExclusive) {
		int merge = startBit ^ endExclusive;
		startBit = Math.min(merge ^ startBit, merge ^ endExclusive);
		endExclusive = merge ^ startBit;
		return getValues(bits, startBit, endExclusive);
	}

	private static int[] getValues(int[] longs, int offset, int endOffset) {
		int maxOffset = longs.length << unit.multOrDivShift();
		endOffset = Math.min(maxOffset, endOffset);
		int totalBits = endOffset - offset;
		int arrSize = (totalBits >>> unit.multOrDivShift()) + ((totalBits & unit.limitMask()) > 0 ? 1 : 0);
		arrSize = Math.min(longs.length, arrSize);

		int[] vals = new int[arrSize];
		for (int i = 0; i < arrSize; i++) {
			int offsetToUse = offset + (i << unit.multOrDivShift());
			vals[i] = getValue(longs, offsetToUse);
		}
		int lastIndexSize = totalBits & unit.limitMask();
		if (lastIndexSize != 0 || lastIndexSize != unit.bits()) {
			int lastIndexMask = -1 >>> (unit.bits() - lastIndexSize);
			vals[vals.length - 1] &= lastIndexMask;
		}
		return vals;
	}

	/**
	 * 
	 * @param longs
	 * @param amount the amount of '0' bits that should be prepended to the first
	 *               value in this array
	 * @return a copy of <code>longs</code> with padding (should not modify passed
	 *         in instance)
	 */
	private static int[] offset(int[] longs, int amount) {
		int numberOfZerosInLastElement = Long.numberOfLeadingZeros(longs[longs.length - 1]);
		int wholeUnits = amount >> unit.multOrDivShift();
		amount -= wholeUnits << unit.multOrDivShift();
		int amountAndZeroDiff = amount - numberOfZerosInLastElement;
		int expand = amountAndZeroDiff > 0 ? 1 : 0;
		int orValSize = (longs.length - 1) + expand;
		int[] orVals = new int[orValSize];
		int orMask = ~(-1 >>> amount);

		int shiftAmt = unit.bits() - amount;

		int[] result = new int[longs.length + wholeUnits + expand];
		System.arraycopy(longs, 0, result, wholeUnits, longs.length);

//		longs.clone();
//		if (orValSize == longs.length) {
//			result = new long[orValSize + 1];
//			System.arraycopy(longs, 0, result, 0, longs.length);
//		}

		for (int i = 0; i < orValSize; i++) {
			int shift = shiftAmt;
			int current = longs[i];
			int orVal = current & orMask;
			orVal >>>= shift;
			orVals[i] = orVal;
		}

		for (int i = 0; i < longs.length; i++) {
			int shift = amount;
			int orVal = 0;
			int orValIdx = i - 1;
			if (orValIdx > -1 && orValIdx < orVals.length) {
				orVal = orVals[orValIdx];
			}
			int resultIdx = i + wholeUnits;
			result[resultIdx] <<= shift;
			result[resultIdx] |= orVal;
		}
		if (expand == 1) {
			result[result.length - 1] = orVals[orVals.length - 1];
		}
		return result;
	}

	private static int getValue(int[] longs, int offset) {
		int idx = offset >>> unit.multOrDivShift();
		int endIdx = (offset + unit.bits()) >>> unit.multOrDivShift();
		int endOffset = (offset + unit.bits()) & unit.limitMask();
		if (idx == endIdx && endOffset == 0) {
			endOffset = unit.bits() - 1;
		}
		int lowerMaskSize = offset & unit.limitMask();
		if (lowerMaskSize == 0) {
			lowerMaskSize = unit.bits();
		}

		int lowerMask = (-1 << lowerMaskSize);
		int upperMask = -1 >>> (unit.bits() - endOffset);// BinaryStrings.toBinaryString(lowerMask,upperMask)
		int value = (longs[idx] & lowerMask) >>> (lowerMaskSize);
		if (Long.numberOfTrailingZeros(lowerMask) != 0 && endIdx < longs.length) {
			value |= (longs[endIdx] & upperMask) << (unit.bits() - endOffset);
		}
		return value;
	}

	private static void oldMain(String[] args) {
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
//		rando.set(148);
		LongQuickBitArray randoClone = rando.clone();
		System.out.println(BinaryStrings.toBinaryString(randoClone.getBitArray(), rando.getSize()));
//		System.out.println(
//				BinaryStrings.toBinaryString(getLowerXBitsInEach(randoClone.getBitArray(), randoClone.getSize(), 31)));
//		bitwiseRotateUsingSubArrays(randoClone.getBitArray(), randoClone.getSize(), 1);
//		System.out.println(randoClone);
//		System.out.println(randoClone);
		System.out.println(BinaryStrings.toBinaryString(randoClone.getBitArray(), randoClone.getSize()));
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
	/**
	 * 
	 * @param bits an <code>int[]</code> that represents bits
	 * @param size the number of <i>bit</i> this <code>int[]</code> represents
	 * @param amt the amount to rotate the bits, a positive value will rotate right, a negative value will rotate left
	 * @return the passed in <code>bits</code> parameter with its bits rotated
	 */
	public static int[] bitwiseRotateOld(int[] bits, int size, int amt) {
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
	private static long derp(long a, long b) {
		return ((((a >>> (unit.bits() >> 1)) ^ (b >>> (unit.bits() >> 1)))
				| ((a & (-1l >>> (unit.bits() >> 1))) ^ (b & (-1l >>> (unit.bits() >> 1))))));
	}

	private static void checkBitHackComps() {
		System.out.println(derp(1, -1));
		System.out.println((int) derp(1, -1));
		System.out.println(aGTb(0, -1));
		System.out.println(aGTb(0, 100));
		System.out.println(aGTb(-1, 0));
		System.out.println(aGTb(5, 4));
		System.out.println(aGTb(-4, 5));
		System.out.println(aGTb(5, -5));
		System.out.println(aGTb(-5, -5));

	}

	public static void main(String[] args) {
		oldMain(args);
	}
}
