package com.jamesratzlaff.util.bit;

import static org.junit.Assert.assertEquals;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.function.ObjIntConsumer;

import org.junit.Test;

import com.jamesratzlaff.util.bit.arrays.IntArrayShift;
import com.jamesratzlaff.util.bit.arrays.QuickBitArray;

public class IntQuickBitArrayTest {
	@FunctionalInterface
	private static interface IntArrayIntIntFunction {
		int[] apply(int[] a, int size, int amt);
	}
	private static ObjIntConsumer<QuickBitArray> createFunctor(String name, IntArrayIntIntFunction func){
		return (QuickBitArray lqba, int shiftAmt)->{
			int size=lqba.getSize();
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
	private static final ObjIntConsumer<QuickBitArray> normal = createFunctor("bitwiseRotate", IntArrayShift::bitwiseRotate);
	private static final ObjIntConsumer<QuickBitArray> subArr = createFunctor("bitwiseRotateUsingSubArrays",IntArrayShift::bitwiseRotateUsingSubArrays);
	private static final ObjIntConsumer<QuickBitArray> old  = createFunctor("bitwiseRotateOld",IntArrayShift::bitwiseRotateOld);
	@SuppressWarnings("unchecked")
	@SafeVarargs
	private void testShift(QuickBitArray lqba, int shiftAmt, ObjIntConsumer<QuickBitArray>...funcs) {
		if(funcs.length==0) {
			funcs= new ObjIntConsumer[]{normal,subArr};
		}
		System.out.println("testing array of len "+lqba.getSize()+" shifting "+shiftAmt);
		
		for(int i=0;i<funcs.length;i++) {
			QuickBitArray manual = lqba.clone();
			ObjIntConsumer<QuickBitArray> currentFunc = funcs[i];
			QuickBitArray lqba2 = lqba.clone();
			shiftAndShiftBackAndCheckEquality(shiftAmt, manual, lqba2, currentFunc);
			assertEquals(lqba,manual);
		}
	}
	private void shiftAndShiftBackAndCheckEquality(int shiftAmt, QuickBitArray manual, QuickBitArray lqba2, ObjIntConsumer<QuickBitArray> currentFunc) {
		shiftAndCheckEquality(shiftAmt, manual, lqba2, currentFunc);
		shiftAndCheckEquality(-shiftAmt, manual, lqba2, currentFunc);
	}
	private void shiftAndCheckEquality(int shiftAmt, QuickBitArray manual, QuickBitArray lqba2, ObjIntConsumer<QuickBitArray> currentFunc) {
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
			QuickBitArray lqba = QuickBitArray.createRandomArrayOfLength(i);
			int shiftAmt = 31;
			testShift(lqba, shiftAmt);
		}
		
	}
	
	@Test
	public void testPowerOfTwoSizeShiftPowerOfTwo() {
		System.out.println("▀ ▁ ▂ ▃ ▄ ▅ ▆ ▇ █ ▉ ▊ ▋ ▌ ▍ ▎ ▏▐ ░ ▒ ▓ ▔ ▕ ▖ ▗ ▘ ▙ ▚ ▛ ▜ ▝ ▞ ▟▪▮");
		for(int i=128;i>0;i>>>=1) {
			QuickBitArray lqba = QuickBitArray.createRandomArrayOfLength(i);
			int shiftAmt = getRandomPowerOf2();
			testShift(lqba, shiftAmt);
		}
		
	}
	
	@Test
	public void testNonPowerOfTwoSizeShiftPowerOfTwo() {
		for(int i=1024;i>0;i>>>=1) {
			QuickBitArray lqba = QuickBitArray.createRandomArrayOfLength(i+13);
			int shiftAmt = getRandomPowerOf2();
			testShift(lqba, shiftAmt);
		}
		
	}
	

	
	

}
