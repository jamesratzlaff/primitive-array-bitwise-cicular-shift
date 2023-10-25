package com.jamesratzlaff.util.bit;

import com.jamesratzlaff.util.bit.converters.ByteArrayConverter;
import com.jamesratzlaff.util.bit.converters.IntArrayConverter;
import com.jamesratzlaff.util.bit.converters.ShortArrayConverter;

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
	
	public static final class ArrayConverters {
		public static final ByteArrayConverter BYTE = new ByteArrayConverter();
		public static final ShortArrayConverter SHORT = new ShortArrayConverter();
		public static final IntArrayConverter INT = new IntArrayConverter();
	}
		
}
