package com.bzsoft.ssr;

import java.awt.image.BufferedImage;

public interface SSRCoder {

	public BufferedImage encode(final BufferedImage img, final byte[] data) throws SSRException;

	public byte[] decode(final BufferedImage img) throws SSRException;

}
