package com.jamesratzlaff.util.bit;

public interface Flerp<T> {
T convertFrom(int[] ints);
int[] convertTo(T vals);
}
