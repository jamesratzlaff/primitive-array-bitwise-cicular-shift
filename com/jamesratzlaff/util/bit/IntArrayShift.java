package com.jamesratzlaff.util.bit;

public class IntArrayShift {

	private static final BitUnit unit = BitUnit.INT;

	public static int[] nonMutatingBitwiseRightShiftCyclic(int[] bits, int size, int amt) {
		int[] copy = new int[bits.length];
		System.arraycopy(bits, 0, copy, 0, bits.length);
		return bitwiseRightShiftCyclic(copy, size, amt);
	}

	public static int[] bitwiseRightShiftCyclic(int[] bits, int size, int amt) {
		int ogAmt=amt;
		amt = normalizeCyclic(amt, size);
		int unitShifts = amt >>> unit.multOrDivShift();
		amt -= (unitShifts << unit.multOrDivShift());
		int maskShifts = unit.bits() - amt;
		int carryMask = maskShifts!=unit.bits()?(-1 >>> maskShifts):0;
		int lastCarryMask = carryMask;
		int lastIndexBits = size & unit.limitMask();
		int bleedOverSize = amt - lastIndexBits;//lastCarryMaskLen;
		if (bleedOverSize < 0) {
			bleedOverSize = 0;
		}
		if (lastIndexBits != 0) {
			lastCarryMask = maskShifts!=unit.bits()?(-1 >>> Math.max(unit.bits()-lastIndexBits, maskShifts)):0;
		}
		if(ogAmt==12) {
			System.out.print("");
		}
		int lastSegmentIndex = bits.length - 1;
		int shiftedLastSegmentIndex = normalizeCyclic(unitShifts + lastSegmentIndex, bits.length);
//		System.out.println("bitsb:\t\t"+toBinaryString(bits, size));
		bits = shiftRight(bits, unitShifts);
		if(ogAmt==54||ogAmt==32) {
			System.out.print("");
		}
		if (shiftedLastSegmentIndex != lastSegmentIndex) {
			int prevIdxGrabMask = ~(-1>>>(unit.bits()-lastIndexBits)) >>> lastIndexBits;
			for (int i = 0; i < bits.length - 1; i++) {
				int currentIndex = normalizeCyclic(shiftedLastSegmentIndex - i, bits.length);
				if(currentIndex==bits.length-1) {
					break;
				}
//				System.out.println("c:" + currentIndex);
				int prevIndex = normalizeCyclic(currentIndex - 1, bits.length);
				int orVal = bits[prevIndex];
				orVal &= prevIdxGrabMask;
				orVal = orVal << lastIndexBits;
				bits[currentIndex] |= orVal;
				bits[prevIndex] = bits[prevIndex] >>> unit.bits()-lastIndexBits;
				

			}
//			System.out.println("shifted: "+toBinaryString(bits,size));
		}
		
//		System.out.println("bitsa:\t\t"+toBinaryString(bits, size));
		int[] orVals=new int[bits.length];
		for (int i = 0; i < bits.length; i++) {
			int mask = carryMask;
			int reshift = maskShifts;
			int nextUnit=i+1;
			if (i == lastSegmentIndex) {
				mask = lastCarryMask;
				reshift = Integer.numberOfLeadingZeros(lastCarryMask);//carryMaskReShiftLen;
				nextUnit=0;
//				carryMasks[0]=carryMasks[0]|bleedOverMask;
			}
			if(i==lastSegmentIndex-1) {
				if(amt<lastIndexBits) {
					reshift=lastIndexBits-(amt%lastIndexBits);
				} else {
					reshift=0;
				}
			}
			int orVal = mask & bits[i];
//			System.out.println("orVal("+i+"):\t"+toBinaryString(orVal));
			orVal = orVal << reshift;
//			System.out.println("sorVal("+i+"):\t"+toBinaryString(orVal));
			orVals[nextUnit] = orVal;
		}
		
//		shiftRight(orVals, 1);
		if(bleedOverSize>0) {
			int bleedOverShifts=unit.bits()-bleedOverSize;
			int bleedGrabMask=(-1>>>bleedOverShifts);
			int grabbedValue=orVals[lastSegmentIndex]&bleedGrabMask;
			orVals[lastSegmentIndex]=orVals[lastSegmentIndex]>>>bleedOverSize;
			orVals[0]=orVals[0]>>>bleedOverSize;
			grabbedValue=(grabbedValue<<bleedOverShifts);
			orVals[0]|=grabbedValue;
		}
		
//		System.out.println("orVals:\t\t"+toBinaryString(orVals));
		for(int i=0;i<bits.length;i++) {
			bits[i]=(bits[i]>>>amt)|orVals[i];
		}
		return bits;
	}
	public static int[] shiftRight(int[] array, int amt) {
		int[] reso = nonMutatingShiftRight(array, amt);
		System.arraycopy(reso, 0, array, 0, array.length);
		return array;
	}

	public static int[] nonMutatingShiftRight(int[] array, int amt) {
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
}
