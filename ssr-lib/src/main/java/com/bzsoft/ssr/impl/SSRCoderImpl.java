package com.bzsoft.ssr.impl;

import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.bzsoft.ssr.SSRCoder;
import com.bzsoft.ssr.SSRException;

public class SSRCoderImpl implements SSRCoder {

	private static final int	IMG_TYPE		= BufferedImage.TYPE_INT_ARGB;
	private static final int	HEADER_SIZE	= 4 * 3;
	private static final int	MAGIC			= 0xBEBACAFE;

	private static int mix(int pixel, int msg) {
		msg = msg & 0x000000ff;
		pixel = pixel & 0xfcfcfcfc;
		int f = msg & 0x03;
		pixel = pixel | f;
		f = (msg & 0x03 << 2) << 6;
		pixel = pixel | f;
		f = (msg & 0x03 << 4) << 12;
		pixel = pixel | f;
		f = (msg & 0x03 << 6) << 18;
		pixel = pixel | f;
		return pixel;
	}

	private static byte parse(int pixel) {
		int msg = 0;
		pixel = pixel & 0x03030303;
		int f = pixel & 0x03;
		msg = msg | f;
		f = (pixel & 0x0300) >> 6;
		msg = msg | f;
		f = (pixel & 0x030000) >> 12;
		msg = msg | f;
		f = (pixel & 0x03000000) >> 18;
		msg = msg | f;
		return (byte) (msg & 0x000000ff);
	}

	private static final byte[] asByteArray(final int i) {
		final byte[] barray = new byte[4];
		barray[0] = (byte) (i & 0x000000ff);
		barray[1] = (byte) (i >> 8 & 0x000000ff);
		barray[2] = (byte) (i >> 16 & 0x000000ff);
		barray[3] = (byte) (i >> 24 & 0x000000ff);
		return barray;
	}

	private static final int asInteger(final byte[] barray) {
		int i = barray[0] & 0x000000ff;
		i = i | barray[1] << 8 & 0x0000ff00;
		i = i | barray[2] << 16 & 0x00ff0000;
		i = i | barray[3] << 24 & 0xff000000;
		return i;
	}

	private static final int digest(final byte[] msg) throws SSRException {
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(msg);
			final byte[] b = md.digest();
			return asInteger(b);
		} catch (final NoSuchAlgorithmException e) {
			throw new SSRException(e.getMessage());
		}
	}

	private static final boolean checkDigest(final byte[] msg, final int digest) throws SSRException {
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(msg);
			final byte[] b = md.digest();
			return asInteger(b) == digest;
		} catch (final NoSuchAlgorithmException e) {
			throw new SSRException(e.getMessage());
		}
	}

	@Override
	public BufferedImage encode(final BufferedImage img, final byte[] data) throws SSRException {
		final int w = img.getWidth();
		final int h = img.getHeight();
		if (data.length > w * h + HEADER_SIZE) {
			throw new SSRException("Not enough space.");
		}
		final BufferedImage out = new BufferedImage(w, h, IMG_TYPE);
		final byte[] buff = new byte[data.length + HEADER_SIZE];
		final byte[] magic = asByteArray(MAGIC);
		final byte[] size = asByteArray(data.length);
		final byte[] digest = asByteArray(digest(data));
		System.arraycopy(magic, 0, buff, 0, magic.length);
		System.arraycopy(size, 0, buff, magic.length, size.length);
		System.arraycopy(digest, 0, buff, magic.length + size.length, digest.length);
		System.arraycopy(data, 0, buff, magic.length + size.length + digest.length, data.length);
		int k = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				final int p = img.getRGB(i, j);
				final byte m;
				final int mixed;
				if (k < buff.length) {
					m = buff[k++];
					mixed = mix(p, m);
				} else {
					mixed = p;
				}
				out.setRGB(i, j, mixed);
			}
		}
		return out;
	}

	@Override
	public byte[] decode(final BufferedImage img) throws SSRException {
		if (img.getType() != IMG_TYPE) {
			throw new SSRException("Image format error (wrong type)");
		}
		final int w = img.getWidth();
		final int h = img.getHeight();
		{
			// magic parsing
			final byte[] magic = new byte[4];
			for (int k = 0; k < magic.length; k++) {
				final int i = k / w;
				final int j = k % h;
				final int pixel = img.getRGB(i, j);
				magic[k] = parse(pixel);
			}
			if (MAGIC != asInteger(magic)) {
				throw new SSRException("Image format error (wrong magic)");
			}
		}
		int size;
		final int digest;
		{
			// size parsing
			final byte[] bsize = new byte[4];
			for (int k = 0; k < bsize.length; k++) {
				final int i = (k + 4) / w;
				final int j = (k + 4) % h;
				final int pixel = img.getRGB(i, j);
				bsize[k] = parse(pixel);
			}
			size = asInteger(bsize);
			if (size > w * h) {
				throw new SSRException("Image format error (wrong size)");
			}
			// digest parsing
			final byte[] bdigest = new byte[4];
			for (int k = 0; k < bdigest.length; k++) {
				final int i = (k + 8) / w;
				final int j = (k + 8) % h;
				final int pixel = img.getRGB(i, j);
				bdigest[k] = parse(pixel);
			}
			digest = asInteger(bdigest);
		}
		// content parsing
		final byte[] content = new byte[size];
		{
			for (int k = 0; k < size; k++) {
				final int i = (k + 12) / w;
				final int j = (k + 12) % h;
				final int pixel = img.getRGB(i, j);
				content[k] = parse(pixel);
			}
		}
		final boolean digestOk = checkDigest(content, digest);
		if (!digestOk) {
			throw new SSRException("Image format error (wrong digest)");
		}
		return content;
	}

}
