package com.jamesratzlaff.util.bit;

import static org.junit.Assert.assertEquals;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.junit.Test;

public class LongQuickBitArrayTest {
	static {
		Random rando=new Random();
		try {
			rando= SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		r=rando;
	}
	
	private static final Random r;
	
	private void testShift(LongQuickBitArray lqba, int shiftAmt) {
		System.out.println("testing array of len "+lqba.getSize()+" shifting "+shiftAmt);
		LongQuickBitArray lqba2 = lqba.clone();
		LongQuickBitArray manual = lqba.clone();
		LongArrayShift.bitwiseRotate(lqba2.getBitArray(), lqba.getSize(), shiftAmt);
		manual.shift(shiftAmt);
		assertEquals(manual, lqba2);
		LongArrayShift.bitwiseRotate(lqba2.getBitArray(), lqba.getSize(), -shiftAmt);
		manual.shift(-shiftAmt);
		assertEquals(manual, lqba2);
		assertEquals(lqba, manual);
	}
	
	private static int getRandomPowerOf2() {
		int amt= 1<<r.nextInt(0, 31);
		if(r.nextBoolean()) {
			amt=-amt;
		}
		return amt;
	}
	
	@Test
	public void testPowerOfTwoSizeShiftNonPowerOfTwo() {
		for(int i=1024;i>0;i>>>=1) {
			LongQuickBitArray lqba = LongQuickBitArray.createRandomArrayOfLength(i);
			int shiftAmt = 31;
			testShift(lqba, shiftAmt);
		}
		
	}
	
	@Test
	public void testPowerOfTwoSizeShiftPowerOfTwo() {
		System.out.println("▀ ▁ ▂ ▃ ▄ ▅ ▆ ▇ █ ▉ ▊ ▋ ▌ ▍ ▎ ▏▐ ░ ▒ ▓ ▔ ▕ ▖ ▗ ▘ ▙ ▚ ▛ ▜ ▝ ▞ ▟▪▮");
		for(int i=128;i>0;i>>>=1) {
			LongQuickBitArray lqba = LongQuickBitArray.createRandomArrayOfLength(i);
			int shiftAmt = getRandomPowerOf2();
			testShift(lqba, shiftAmt);
		}
		
	}
	
	@Test
	public void testNonPowerOfTwoSizeShiftPowerOfTwo() {
		for(int i=1024;i>0;i>>>=1) {
			LongQuickBitArray lqba = LongQuickBitArray.createRandomArrayOfLength(i+13);
			int shiftAmt = getRandomPowerOf2();
			testShift(lqba, shiftAmt);
		}
		
	}
	

	
	
}
