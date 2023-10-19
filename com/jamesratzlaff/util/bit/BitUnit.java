package com.jamesratzlaff.util.bit;

public enum BitUnit implements IBitUnit {
	BYTE(Byte.SIZE),SHORT(Short.SIZE),INT(Integer.SIZE),LONG(Long.SIZE);
	
	
	private final int bits;
	private final int logBase2;
	private final int limitMask;
	
	private BitUnit(int bits) {
		this.bits=bits;
		this.logBase2=IBitUnit.super.log2();
		this.limitMask=IBitUnit.super.limitMask();
	}

	@Override
	public int bits() {
		return this.bits;
	}

	@Override
	public int limitMask() {
		return this.limitMask;
	}

	@Override
	public int log2() {
		return this.logBase2;
	}
		
}
