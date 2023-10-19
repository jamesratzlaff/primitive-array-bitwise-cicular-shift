package com.jamesratzlaff.util.bit;

@FunctionalInterface
public interface IBitUnit {

	int bits();
	default int limitMask() {
		return bits()-1;
	}
	default int log2() {
		return (int)(Math.log(bits())/Math.log(2));
	}
	default int multOrDivShift() {
		return log2();
	}
	
	default int per(BitUnit other) {
		return other.bits()>>>this.multOrDivShift();
	}
	default long modulandL(long value) {
		return value&limitMask();
	}
	default int moduland(int value) {
		return value&limitMask();
	}
	default int mult(int value) {
		return value<<multOrDivShift();
	}
	default long multL(long value) {
		return value<<multOrDivShift();
	}
	default int udiv(int value) {
		return value>>>multOrDivShift();
	}
	default long udivL(long value) {
		return value>>>multOrDivShift();
	}
	default int div(int value) {
		return value>>multOrDivShift();
	}
	default long divL(long value) {
		return value>>multOrDivShift();
	}
}
