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
	

	/**
	 * This actually operates more like getting the carries for left shifting btw
	 * @param bits
	 * @param size
	 * @param amt
	 * @return
	 */
	private static long[] getOrValsForRightShift(long[] bits, int size, int amt) {
		if(amt==0) {
			return new long[bits.length];
		}
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
		return shiftRight(bits, size, amt, false, false);
	}

	
	private static long[] shiftRight(long[] bits, int size, int amt, boolean discardLeftCarry, boolean discardRightCarry) {
		

		amt = normalizeCyclicI(amt, size);
		int unitShifts = amt >>> unit.multOrDivShift();
		amt -= (unitShifts << unit.multOrDivShift());
		unitShifts=unitShifts%bits.length;
		int numberOfLastBits = size&unit.limitMask();
		
		long lastBitsMask = -1l>>>unit.bits()-numberOfLastBits;
		
		if(unitShifts!=0) {
			rotateAndCollapseForRightShift(bits, size, unitShifts);
		}
		long[] orVals = getOrValsForRightShift(bits, size, amt);
		rotate(orVals, 1);
		if(discardLeftCarry) {
			orVals[0]=0;
		}
		if(discardRightCarry) {
			orVals[orVals.length-1]=0;
		}
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
		return getValues(bits, startBit, endExclusive);
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

	
	private static long[] getValues(long[] longs, int offset, int endOffset) {
		int maxOffset = longs.length<<unit.multOrDivShift();
		endOffset=Math.min(maxOffset, endOffset);
		int totalBits=endOffset-offset;
		int arrSize = (totalBits>>>unit.multOrDivShift())+((totalBits&unit.limitMask())>0?1:0);
		arrSize=Math.min(longs.length, arrSize);
		
		long[] vals=new long[arrSize];
		for(int i=0;i<arrSize;i++) {
			int offsetToUse = offset+(i<<unit.multOrDivShift());
			vals[i]=getValue(longs, offsetToUse);
		}
		int lastIndexSize = totalBits&unit.limitMask();
		if(lastIndexSize!=0||lastIndexSize!=unit.bits()) {
			long lastIndexMask = -1l>>>(unit.bits()-lastIndexSize);
			vals[vals.length-1]&=lastIndexMask;
		}
		return vals;
	}
	
	
	/**
	 * 
	 * @param longs
	 * @param amount the amount of '0' bits that should be prepended to the first value in this array 
	 * @return
	 */
	private static long[] offset(long[] longs, int amount) {
		int numberOfZerosInLastElement=Long.numberOfLeadingZeros(longs[longs.length-1]);
		int wholeUnits=amount>>unit.multOrDivShift();
		amount-=wholeUnits<<unit.multOrDivShift();
		int amountAndZeroDiff=amount-numberOfZerosInLastElement;
		int expand = amountAndZeroDiff>0?1:0;
		int orValSize = (longs.length-1)+expand;
		long[] orVals=new long[orValSize];
		long orMask=~(-1l>>>amount);

		long[] result=longs;
		if(orValSize==longs.length) {
			result=new long[orValSize+1];
			System.arraycopy(longs, 0, result, 0, longs.length);
		}

		for(int i=0;i<orValSize;i++) {
			int shift=(unit.bits()-amount); 
			long current=longs[i];
			long orVal = current&orMask;
			orVal>>>=shift;
			orVals[i]=orVal;
		}
	
		for(int i=0;i<longs.length;i++) {
			int shift=amount;
			long orVal=0;
			int orValIdx=i-1;
			if(orValIdx>-1&&orValIdx<orVals.length) {
				orVal=orVals[i-1];
			}
			result[i]<<=shift;
			result[i]|=orVal;
		}
		if(longs.length<result.length) {
			result[result.length-1]=orVals[orVals.length-1];
		}
		return result;
	}
	
	private static long getValue(long[] longs, int offset) {
		int idx = offset>>>unit.multOrDivShift();
		int endIdx = (offset+unit.bits())>>>unit.multOrDivShift();
		int endOffset=(offset+unit.bits())&unit.limitMask();
		if(idx==endIdx&&endOffset==0) {
			endOffset=unit.bits()-1;
		}
		long lowerMaskSize=offset&unit.limitMask();
		if(lowerMaskSize==0) {
			lowerMaskSize=unit.bits();
		}
	
		long lowerMask=(-1l<<lowerMaskSize);
		long upperMask=-1l>>>(unit.bits()-endOffset);//BinaryStrings.toBinaryString(lowerMask,upperMask)
		long value = (longs[idx]&lowerMask)>>>(lowerMaskSize);
		if(Long.numberOfTrailingZeros(lowerMask)!=0&&endIdx<longs.length) {
			value|=(longs[endIdx]&upperMask)<<(unit.bits()-endOffset);
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
		System.out.println(BinaryStrings.toBinaryString(rando.getBitArray()));
		long[] sub = getValues(rando.getBitArray(), 3, 149);
		System.out.println(BinaryStrings.toBinaryString(sub));
		System.out.println(BinaryStrings.toBinaryString(offset(sub,63)));
		System.out.println(rando);
		
		System.out.println(rando);
		long[] subs = subBits(rando.getBitArray(), 1, 68);
		LongQuickBitArray subArray = new LongQuickBitArray(subs, 67);
		System.out.println(subArray);
//		System.out.println(BinaryStrings.toBinaryString(subs));


	}
}
