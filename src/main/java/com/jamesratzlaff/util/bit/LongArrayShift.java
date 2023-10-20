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
		long carryMask = maskShifts!=unit.bits()?(-1l >>> maskShifts):0l;
		long lastCarryMask = carryMask;
		long lastIndexBits = size & unit.limitMask();
		final long lastCarryMaskShift = unit.bits()-lastIndexBits;
		long bleedOverSize = amt - lastIndexBits;//lastCarryMaskLen;
		if (bleedOverSize < 0) {
			bleedOverSize = 0l;
		}
		if (lastIndexBits != 0) {
			lastCarryMask = maskShifts!=unit.bits()?(-1l >>> Math.max(lastCarryMaskShift, maskShifts)):0l;
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
	private static void printB(long val) {
		System.out.println(toBin(val));
	}
	private static String toBin(long val) {
		return toBin(null,null,val);
	}
	private static void printB(String label, long val) {
		System.out.println(toBin(label,val));
	}
	private static String toBin(String label, long val) {
		return toBin(label,null,val);
	}
	private static void printB(String label,String delim, long val) {
		System.out.println(toBin(label,delim,val));
	}
	private static String toBin(String label, String delim, long val) {
		StringBuilder sb = new StringBuilder();
		if(label!=null) {
			sb.append(label);
			if(!label.endsWith(":")) {
				sb.append(':');
			}
			if(delim!=null) {
				sb.append(delim);
			} else {
				sb.append("\t");
			}
		}
		sb.append(BinaryStrings.toBinaryString(val));
		return sb.toString();
	}
	private static String toBin(String label, String delim, long[] val) {
		return toBin(label,delim,val,-1);
	}
	private static String toBin(String label, String delim, long[] val, int size) {
		StringBuilder sb = new StringBuilder();
		if(label!=null) {
			sb.append(label);
			if(delim!=null) {
				sb.append(delim);
			} else {
				sb.append("\t");
			}
		}
		sb.append(BinaryStrings.toBinaryString(val,size));
		return sb.toString();
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
	
	public static long[] subBits(long[] bits,int startBit, int endExclusive) {
		int merge=startBit^endExclusive;
		startBit=Math.min(merge^startBit, merge^endExclusive);
		endExclusive=merge^startBit;
		int startIdx=getArrayIdxUsingBitIdx(startBit);
		int endIdx=getArrayIdxUsingBitIdx(endExclusive);
		int len=(endIdx-startIdx)+1;
		long[] reso=new long[len];
		System.arraycopy(bits, startIdx, reso, 0, len);
		int reducedSize=endExclusive-startBit;
		endExclusive=(endExclusive-startBit)&unit.limitMask();
		startBit=startBit&unit.limitMask();
		shiftLeft(reso,startBit);
		reso[len-1]=reso[len-1]>>>(unit.bits()-endExclusive);
		return reso;
	}
	
	private static void shiftLeft(long[] bits,int amt) {
		int size=unit.bits()*bits.length;
		amt=normalizeCyclicI(amt,size);
		long orMask = -1l<<(unit.bits()-amt);
		int shiftAmt=amt;
		int numberOfLastBits = size&unit.limitMask();
		long lastMaskSize = Math.min(amt, numberOfLastBits);
		long lastMaskShifts = (unit.bits()-lastMaskSize); 
		long lastMask=-1l>>>lastMaskShifts;
		int lastIdx=bits.length-1;
		int lastIter=lastIdx-1;
		for(int i=0;i<bits.length;i++) {
			long current=bits[i];
			current=current<<shiftAmt;
			if(i<lastIdx) {
			long orVal=(orMask&bits[i+1]);
				current|=orVal;
			}
			bits[i]=current;
		}
	}
	
	private static int getArrayIdxUsingBitIdx(int bitIdx) {
		return bitIdx>>>unit.multOrDivShift();
	}
	
	private static List<String> getArraySubArrayComparisonStrings(long[] bits, int size, int subStart, int subEnd) {
		List<String> strs=new ArrayList<String>(2);
		strs.add(BinaryStrings.toBinaryString(bits, size, ""));
		char[] spaces = new char[subStart];
		Arrays.fill(spaces, ' ');
		long[] subArray = subBits(bits, subStart, subEnd);
		strs.add(new String(spaces)+BinaryStrings.toBinaryString(subArray,subEnd-subStart,""));
		return strs;
	}
	
	private static void printSubArrayComparison(long[] bits, int size, int subStart, int subEnd) {
		List<String> strs = getArraySubArrayComparisonStrings(bits, size, subStart, subEnd);
		System.out.println(String.join("\n", strs));
	}
	
	public static void main(String[] args) {
		int bitLen=233;
		LongQuickBitArray lqba = LongQuickBitArray.createRandomArrayOfLength(bitLen);
		System.out.println(lqba);
		String asStr=BinaryStrings.toBinaryString(lqba.getBitArray(),lqba.getSize(),"");
		asStr=asStr.substring(1,asStr.length()-1);
		StringBuilder sb = new StringBuilder();
		
		System.out.println(BinaryStrings.toBinaryString(lqba.getBitArray(),lqba.getSize(),"").replace('1','█').replace('0', '_'));
		int start=1;
		int end=68;
		long[] bits=lqba.getBitArray();
		printSubArrayComparison(bits, bitLen, start, end);
		
		
		
//		System.out.println("-1:\t"+BinaryStrings.toBinaryString(lqba.getBitArray(),lqba.getSize()));
//		int rotations=(bitLen*4)+12;
//		for(int i=0;i<rotations;i++) {
//			System.out.println(i+":\t"+BinaryStrings.toBinaryString(nonMutatingBitwiseRotate(lqba.getBitArray(), lqba.getSize(), i),lqba.getSize()));
//		}
		
		
		
	}
}
