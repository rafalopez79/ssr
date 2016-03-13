package com.bzsoft.ssr.impl;

public final class StreamUtil {

	private static final byte a = (byte)'a';
	private static final byte z = (byte)'z';
	private static final byte A = (byte)'A';
	private static final byte Z = (byte)'Z';

	private StreamUtil(){
		//empty
	}

	public static final byte rot13(final byte b){
		if (b >= a && b <= z){
			final byte c = (byte) (b + 13);
			if (c > z){
				return (byte) (a + c - z);
			}
			return c;
		}else if (b >= A && b <=Z){
			final byte c = (byte) (b + 13);
			if (c > Z){
				return (byte) (A + c - Z);
			}
			return c;
		}
		return b;
	}

}
