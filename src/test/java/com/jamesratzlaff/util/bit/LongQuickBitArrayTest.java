package com.jamesratzlaff.util.bit;

import static org.junit.Assert.assertEquals;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.function.ObjIntConsumer;

import org.junit.Test;

public class LongQuickBitArrayTest {
	@FunctionalInterface
	private static interface LongArrayLongLongFunction {
		long[] apply(long[] a, long size, long amt);
	}
	private static ObjIntConsumer<LongQuickBitArray> createFunctor(String name, LongArrayLongLongFunction func){
		return (LongQuickBitArray lqba, int shiftAmt)->{
			long size=lqba.getSize();
			System.out.println("Running "+name+" with array of len "+size+" shifting "+shiftAmt);
			func.apply(lqba.getBitArray(), size, shiftAmt);
		};
	}
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
	private static final ObjIntConsumer<LongQuickBitArray> normal = createFunctor("bitwiseRotate", LongArrayShift::bitwiseRotate);
	private static final ObjIntConsumer<LongQuickBitArray> inlined = createFunctor("bitwiseRotateInlined",LongArrayShift::bitwiseRotateInlined);
	
	@SuppressWarnings("unchecked")
	@SafeVarargs
	private void testShift(LongQuickBitArray lqba, int shiftAmt, ObjIntConsumer<LongQuickBitArray>...funcs) {
		if(funcs.length==0) {
			funcs= new ObjIntConsumer[]{normal,inlined};
		}
		System.out.println("testing array of len "+lqba.getSize()+" shifting "+shiftAmt);
		
		for(int i=0;i<funcs.length;i++) {
			LongQuickBitArray manual = lqba.clone();
			ObjIntConsumer<LongQuickBitArray> currentFunc = funcs[i];
			LongQuickBitArray lqba2 = lqba.clone();
			shiftAndShiftBackAndCheckEquality(shiftAmt, manual, lqba2, currentFunc);
			assertEquals(lqba,manual);
		}
	}
	private void shiftAndShiftBackAndCheckEquality(int shiftAmt, LongQuickBitArray manual, LongQuickBitArray lqba2, ObjIntConsumer<LongQuickBitArray> currentFunc) {
		shiftAndCheckEquality(shiftAmt, manual, lqba2, currentFunc);
		shiftAndCheckEquality(-shiftAmt, manual, lqba2, currentFunc);
	}
	private void shiftAndCheckEquality(int shiftAmt, LongQuickBitArray manual, LongQuickBitArray lqba2, ObjIntConsumer<LongQuickBitArray> currentFunc) {
		currentFunc.accept(lqba2, shiftAmt);
		manual.shift(shiftAmt);
		assertEquals(manual, lqba2);
		
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
