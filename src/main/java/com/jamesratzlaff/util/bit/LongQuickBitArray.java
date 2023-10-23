package com.jamesratzlaff.util.bit;

import java.util.Arrays;
import java.util.Random;

public class LongQuickBitArray {

	public static final int BITS_PER_BYTE=BitUnit.BYTE.bits();//8
	public static final int BITS_PER_BYTE_SHIFT=BitUnit.BYTE.multOrDivShift();//(1<<BITS_PER_BYTE_SHIFT)==BITS_PER_BYTE;
	public static final int BIT_SHIFT_BYTE_LIMIT_MASK=BitUnit.BYTE.limitMask();
	public static final int BITS_PER_UNIT=BitUnit.LONG.bits();//64
	public static final int BITS_PER_UNIT_SHIFT=BitUnit.LONG.multOrDivShift();//(1<<BITS_PER_UNIT_SHIFT)==BITS_PER_UNIT;
	public static final int BIT_SHIFT_UNIT_LIMIT_MASK=BitUnit.LONG.limitMask();//for ints this would be 31 (0x1F) and for longs it would be 63 (0x3F)

	private static final long[] EMPTY_BITS = {};
	private static final float LOAD_FACTOR = 0.75f;

	private long[] bits;
	private int size;

//	/**
//	 * 
//	 * @param ba
//	 * @return an instance of {@link QuickBitArray} in which any mutations are directly affected in the given {@link BitArray}
//	 */
//	public static QuickBitArray wrap(BitArray ba) {
//		return new QuickBitArray(ba);
//	}
//	/**
//	 * 
//	 * @param ba
//	 * @return an instance of {@link QuickBitArray} that is created from a copy of the given {@link BitArray}
//	 */
//	public static QuickBitArray fromBitArray(BitArray ba) {
//		return wrap(ba.clone());
//	}
//	
//	private QuickBitArray(BitArray ba) {
//		this(ba.getBitArray(),ba.getSize());
//	}
	
	public LongQuickBitArray() {
		this.size = 0;
		this.bits = EMPTY_BITS;
	}

	public LongQuickBitArray(int size) {
		this.size = size;
		this.bits = makeArray(size);
	}

	// For testing only
	public LongQuickBitArray(long[] bits, int size) {
		this.bits = bits;
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public int getSizeInBytes() {
		return (size + BIT_SHIFT_BYTE_LIMIT_MASK) >>> BITS_PER_BYTE_SHIFT;
	}

	private void ensureCapacity(int newSize) {
		if (newSize > (bits.length << BITS_PER_UNIT_SHIFT)) {
			long[] newBits = makeArray((int) Math.ceil(newSize / LOAD_FACTOR));
			System.arraycopy(bits, 0, newBits, 0, bits.length);
			this.bits = newBits;
		}
	}

	/**
	 * @param i bit to get
	 * @return true iff bit i is set
	 */
	public boolean get(int i) {
		return (bits[(i >>> BITS_PER_UNIT_SHIFT)] & (1l << ((long)i & BIT_SHIFT_UNIT_LIMIT_MASK))) != 0;
	}

	/**
	 * Sets bit i.
	 *
	 * @param i bit to set
	 */
	public void set(int i) {
		bits[(i >>> BITS_PER_UNIT_SHIFT)] |= (long)1l << ((long)i & BIT_SHIFT_UNIT_LIMIT_MASK);
	}
	
	public void unset(int i) {
		bits[(i >>> BITS_PER_UNIT_SHIFT)] = 0l;
	}

	/**
	 * Flips bit i.
	 *
	 * @param i bit to set
	 */
	public void flip(int i) {
		bits[(i >>> BITS_PER_UNIT_SHIFT)] ^= 1l << (i & BIT_SHIFT_UNIT_LIMIT_MASK);
	}

	/**
	 * @param from first bit to check
	 * @return index of first bit that is set, starting from the given index, or
	 *         size if none are set at or beyond this given index
	 * @see #getNextUnset(int)
	 */
	public int getNextSet(int from) {
		if (from >= size) {
			return size;
		}
		int bitsOffset = (from >>> BITS_PER_UNIT_SHIFT);
		long currentBits = bits[bitsOffset];
		// mask off lesser bits first
		currentBits &= -(1l << (from & BIT_SHIFT_UNIT_LIMIT_MASK));
		while (currentBits == 0) {
			if (++bitsOffset == bits.length) {
				return size;
			}
			currentBits = bits[bitsOffset];
		}
		int result = (bitsOffset << BITS_PER_UNIT_SHIFT) + Long.numberOfTrailingZeros(currentBits);
		return Math.min(result, size);
	}

	/**
	 * @param from index to start looking for unset bit
	 * @return index of next unset bit, or {@code size} if none are unset until the
	 *         end
	 * @see #getNextSet(int)
	 */
	public int getNextUnset(int from) {
		if (from >= size) {
			return size;
		}
		int bitsOffset = (from >>> BITS_PER_UNIT_SHIFT);
		long currentBits = ~bits[bitsOffset];
		// mask off lesser bits first
		currentBits &= -(1l << (from & BIT_SHIFT_UNIT_LIMIT_MASK));
		while (currentBits == 0) {
			if (++bitsOffset == bits.length) {
				return size;
			}
			currentBits = ~bits[bitsOffset];
		}
		int result = (bitsOffset << BITS_PER_UNIT_SHIFT) + Long.numberOfTrailingZeros(currentBits);
		return Math.min(result, size);
	}

	/**
	 * Sets a block of 32 bits, starting at bit i.
	 *
	 * @param i       first bit to set
	 * @param newBits the new value of the next 64 bits. Note again that the
	 *                least-significant bit corresponds to bit i, the
	 *                next-least-significant to i+1, and so on.
	 */
	public void setBulk(int i, long newBits) {
		bits[(i >>> BITS_PER_UNIT_SHIFT)] = newBits;
	}

	/**
	 * Sets a range of bits.
	 *
	 * @param start start of range, inclusive.
	 * @param end   end of range, exclusive
	 */
	public void setRange(int start, int end) {
		if (end < start || start < 0 || end > size) {
			throw new IllegalArgumentException();
		}
		if (end == start) {
			return;
		}
		end--; // will be easier to treat this as the last actually set bit -- inclusive
		int firstInt = (start >>> BITS_PER_UNIT_SHIFT);
		int lastInt = (end >>> BITS_PER_UNIT_SHIFT);
		for (int i = firstInt; i <= lastInt; i++) {
			int firstBit = i > firstInt ? 0 : start & BIT_SHIFT_UNIT_LIMIT_MASK;
			int lastBit = i < lastInt ? BIT_SHIFT_UNIT_LIMIT_MASK : end & BIT_SHIFT_UNIT_LIMIT_MASK;
			// Ones from firstBit to lastBit, inclusive
			long mask = (2l << lastBit) - (1l << firstBit);
			bits[i] |= mask;
		}
	}

	/**
	 * Clears all bits (sets to false).
	 */
	public void clear() {
		int max = bits.length;
		for (int i = 0; i < max; i++) {
			bits[i] = 0;
		}
	}

	/**
	 * Efficient method to check if a range of bits is set, or not set.
	 *
	 * @param start start of range, inclusive.
	 * @param end   end of range, exclusive
	 * @param value if true, checks that bits in range are set, otherwise checks
	 *              that they are not set
	 * @return true iff all bits are set or not set in range, according to value
	 *         argument
	 * @throws IllegalArgumentException if end is less than start or the range is
	 *                                  not contained in the array
	 */
	public boolean isRange(int start, int end, boolean value) {
		if (end < start || start < 0 || end > size) {
			throw new IllegalArgumentException();
		}
		if (end == start) {
			return true; // empty range matches
		}
		end--; // will be easier to treat this as the last actually set bit -- inclusive
		int firstInt = (start >>> BITS_PER_UNIT_SHIFT);
		int lastInt = (end >>> BITS_PER_UNIT_SHIFT);
		for (int i = firstInt; i <= lastInt; i++) {
			int firstBit = i > firstInt ? 0 : start & BIT_SHIFT_UNIT_LIMIT_MASK;
			int lastBit = i < lastInt ? BIT_SHIFT_UNIT_LIMIT_MASK : end & BIT_SHIFT_UNIT_LIMIT_MASK;
			// Ones from firstBit to lastBit, inclusive
			long mask = (2l << lastBit) - (1l << firstBit);

			// Return false if we're looking for 1s and the masked bits[i] isn't all 1s
			// (that is,
			// equals the mask, or we're looking for 0s and the masked portion is not all 0s
			if ((bits[i] & mask) != (value ? mask : 0l)) {
				return false;
			}
		}
		return true;
	}

	public void appendBit(boolean bit) {
		ensureCapacity(size + 1);
		if (bit) {
			bits[(size >>> BITS_PER_UNIT_SHIFT)] |= 1 << (size & BIT_SHIFT_UNIT_LIMIT_MASK);
		}
		size++;
	}

	/**
	 * Appends the least-significant bits, from value, in order from
	 * most-significant to least-significant. For example, appending 6 bits from
	 * 0x000001E will append the bits 0, 1, 1, 1, 1, 0 in that order.
	 *
	 * @param value   {@code int} containing bits to append
	 * @param numBits bits from value to append
	 */
	public void appendBits(int value, int numBits) {
		if (numBits < 0 || numBits > BITS_PER_UNIT) {
			throw new IllegalArgumentException("Num bits must be between 0 and "+BITS_PER_UNIT);
		}
		int nextSize = size;
		ensureCapacity(nextSize + numBits);
		for (int numBitsLeft = numBits - 1; numBitsLeft >= 0; numBitsLeft--) {
			if ((value & (1l << numBitsLeft)) != 0) {
				bits[(nextSize >>> BITS_PER_UNIT_SHIFT)] |= 1l << (nextSize & BIT_SHIFT_UNIT_LIMIT_MASK);
			}
			nextSize++;
		}
		size = nextSize;
	}

	public void appendBitArray(LongQuickBitArray other) {
		int otherSize = other.size;
		ensureCapacity(size + otherSize);
		for (int i = 0; i < otherSize; i++) {
			appendBit(other.get(i));
		}
	}

	public void xor(LongQuickBitArray other) {
		if (size != other.size) {
			throw new IllegalArgumentException("Sizes don't match");
		}
		for (int i = 0; i < bits.length; i++) {
			// The last int could be incomplete (i.e. not have 32 bits in
			// it) but there is no problem since 0 XOR 0 == 0.
			bits[i] ^= other.bits[i];
		}
	}

	/**
	 *
	 * @param bitOffset first bit to start writing
	 * @param array     array to write into. Bytes are written most-significant byte
	 *                  first. This is the opposite of the internal representation,
	 *                  which is exposed by {@link #getBitArray()}
	 * @param offset    position in array to start writing
	 * @param numBytes  how many bytes to write
	 */
	public void toBytes(int bitOffset, byte[] array, int offset, int numBytes) {
		for (int i = 0; i < numBytes; i++) {
			int theByte = 0;
			for (int j = 0; j < BITS_PER_BYTE; j++) {
				if (get(bitOffset)) {
					theByte |= 1l << (BIT_SHIFT_BYTE_LIMIT_MASK - j);
				}
				bitOffset++;
			}
			array[offset + i] = (byte) theByte;
		}
	}

	/**
	 * @return underlying array of ints. The first element holds the first 32 bits,
	 *         and the least significant bit is bit 0.
	 */
	public long[] getBitArray() {
		return bits;
	}

	/**
	 * Reverses all bits in the array.
	 */
	public void reverse() {
		long[] newBits = new long[bits.length];
		// reverse all int's first
		int len = ((size - 1) >>> BITS_PER_UNIT_SHIFT);
		int oldBitsLen = len + 1;
		for (int i = 0; i < oldBitsLen; i++) {
			newBits[len - i] = Long.reverse(bits[i]);
		}
		// now correct the int's if the bit size isn't a multiple of 32
		if (size != (oldBitsLen << BITS_PER_UNIT_SHIFT)) {
			long leftOffset = (oldBitsLen << BITS_PER_UNIT_SHIFT) - size;
			long currentInt = newBits[0] >>> leftOffset;
			for (int i = 1; i < oldBitsLen; i++) {
				long nextInt = newBits[i];
				currentInt |= nextInt << (BITS_PER_UNIT - leftOffset);
				newBits[i - 1] = currentInt;
				currentInt = nextInt >>> leftOffset;
			}
			newBits[oldBitsLen - 1] = currentInt;
		}
		bits = newBits;
	}

	private static long[] makeArray(int size) {
		return new long[((size + BIT_SHIFT_UNIT_LIMIT_MASK) >>> BITS_PER_UNIT_SHIFT)];
	}
	
	/**
	 * I have no clue why this does the opposite of what I expect...I figured it out, the binary representation is reversed I guess
	 * @param amount if negative it shifts left, positive right...is cyclic
	 */
	public LongQuickBitArray shift(int amount) {
		int absAmt = Math.abs(amount);
		absAmt=absAmt%this.size;
//		System.out.println("0:\t"+BinaryStrings.toBinaryString(this.bits,this.size));
		if (amount != 0) {
			if (amount < 0) {
				for (int i = 0; i < absAmt; i++) {
					shiftLeft();
				}
			} else {
				for (int i = 0; i < absAmt; i++) {
					shiftRight();
				}
				
			}
		}
		return this;
//		amount=amount%this.getSize();
//		if(amount!=0) {
//			int discreteAmt =amount<0?-1:1;
//			int times=discreteAmt*amount;
//			IntBinaryOperator op = amount<0?SHIFT_LEFT:SHIFT_RIGHT;
//			for(int i=0;i<times;i++) {
//				int[] carryMasks=getCarryMasksForOring(amount, this);
//				for(int j=0;j<this.bits.length;j++) {
//					int shifted=op.applyAsInt(this.bits[j], discreteAmt);
//					int carryMask=carryMasks[j];
//					int shiftedWithCarry=(shifted|carryMask);
//					this.bits[j]=shiftedWithCarry;
//				}
//			}
//		}
//		return this;
	}
	
	
	
	
	public LongQuickBitArray shiftRight() {
		LongQuickBitArray clone=new LongQuickBitArray(this.getSize());
		for(int i=0;i<this.size-1;i++) {
			if(this.get(i)) {
				clone.set(i+1);
			}
		}
		if(this.get(this.size-1)) {
			clone.set(0);
		}
		for(int i=0;i<clone.getBitArray().length;i++) {
			this.getBitArray()[i]=clone.getBitArray()[i];
		}
		
		return this;
	}
	
	public LongQuickBitArray shiftLeft() {
		LongQuickBitArray clone=new LongQuickBitArray(this.getSize());
		if(this.get(0)) {
			clone.set(clone.getSize()-1);
		}
		for(int i=1;i<this.size;i++) {
			if(this.get(i)) {
				clone.set(i-1);
			} 
		}
		for(int i=0;i<clone.getBitArray().length;i++) {
			this.bits=clone.bits;
		}
		return this;
	}
	
//	
//	private static final int rightShiftNeedsCarryMask=1;
//	private static final int leftShiftNeedsCarryMask=Integer.MIN_VALUE;
//	private static final int[] regularMasks= {leftShiftNeedsCarryMask,rightShiftNeedsCarryMask};
//	private static final int[] increments = {-1,1};
//	private static IntUnaryOperator NOT = (i)->((~i)&1);
//	
//	private static long[] getCarryMasksForOring(int amount, QuickBitArray qba) {
//		return getCarryMasksForOring(amount, qba.getBitArray(), qba.getSize());
//	}
//	
//	
//	private static int[] getCarryMasksForOring(int amount,int[] ints, int sizeInBits) {
//		int normalizedAmount=(amount&(BIT_SHIFT_UNIT_LIMIT_MASK));
//		
//		boolean shiftLeft=amount<0;
//		int optIdx = shiftLeft?0:1;
//		int opposite=NOT.applyAsInt(optIdx);
//		int checkMask = regularMasks[optIdx];
//		int orMask=regularMasks[opposite];
//		int rightMostBitMask=getRightMostIntBitMask(sizeInBits);
//		int[] rollMasks= {leftShiftNeedsCarryMask,rightMostBitMask};
//		
//		int rightMostCheckMask=rollMasks[optIdx];//shiftLeft?leftShiftNeedsCarryMask:rightMostBitMask;
//		int rightMostOrMask=rollMasks[opposite];//shiftLeft?rightMostBitMask:leftShiftNeedsCarryMask;
//		int maxIndex=ints.length-1;
//		int[] starts = {maxIndex,0};
//		int start=starts[optIdx];//shiftLeft?maxIndex:0;
//		IntPredicate[] endConds= {(i)->i>-1,(i)->i<ints.length};
//		IntPredicate endCond=endConds[optIdx];//shiftLeft?(i)->i>-1:(i)->i<ints.length;
//		int increment=increments[optIdx];//shiftLeft?-1:1;
//		int[] rollIndexes= {0,maxIndex};
//		
//		int rollFromIndex=rollIndexes[optIdx];//shiftLeft?0:maxIndex;
//		int rollToIndex=rollIndexes[opposite];//shiftLeft?maxIndex:0;
//		
//		int[] result=new int[ints.length];
//		for(int i=start;endCond.test(i);i+=increment) {
//			int current=ints[i];
//			boolean isRoll=i==rollFromIndex;
//			int orAssignIndex=i+increment;
//			int cMask=checkMask;
//			int oMask=orMask;
//			if(isRoll) {
//				orAssignIndex=rollToIndex;
//				cMask=rightMostCheckMask;
//				oMask=rightMostOrMask;
//			}
//			boolean carry = (cMask&current)==cMask;
//			if(carry) {
//				result[orAssignIndex]=oMask;
//			}
//		}
//		return result;
//	}
//	
//	private static final int getRightMostIntBitMask(int size) {
//		int shiftAmount = (BITS_PER_UNIT-(size&BIT_SHIFT_UNIT_LIMIT_MASK));
//		return 1<<shiftAmount;
//	}
	
	private static LongQuickBitArray randomFill(LongQuickBitArray qba) {
		Random r=new Random();
		for(int i=0;i<qba.getSize();i++) {
			boolean t = r.nextBoolean();
			if(t) {
				qba.set(i);
			}
		}
		return qba;
	}

	public static LongQuickBitArray createRandomArrayOfLength(int len) {
		LongQuickBitArray qba = new LongQuickBitArray(len);
		return randomFill(qba);
	}
	
	public static void main(String[] args) {
		LongQuickBitArray qba = createRandomArrayOfLength(75);
		System.out.println(qba);
		System.out.println(qba.shiftLeft());
		System.out.println(qba.shiftLeft());
		System.out.println(qba.shiftRight());
	}
	

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LongQuickBitArray)) {
			return false;
		}
		LongQuickBitArray other = (LongQuickBitArray) o;
		return size == other.size && Arrays.equals(bits, other.bits);
	}

	@Override
	public int hashCode() {
		return 31 * size + Arrays.hashCode(bits);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(size + (size >>> BITS_PER_BYTE_SHIFT) + 1);
		for (int i = 0; i < size; i++) {
			if ((i & BIT_SHIFT_BYTE_LIMIT_MASK) == 0) {
				result.append(' ');
			}
			result.append(get(i) ? "█" : "_");
		}
		return result.toString();
	}
	
	public static LongQuickBitArray from(QuickBitArray qba) {
		LongQuickBitArray lqba=new LongQuickBitArray(qba.getSize());
		for(int i=0;i<qba.getSize();i++) {
			if(qba.get(i)) {
				lqba.set(i);
			}
		}
		return lqba;
	}

	@Override
	public LongQuickBitArray clone() {
		return new LongQuickBitArray(bits.clone(), size);
	}
}
