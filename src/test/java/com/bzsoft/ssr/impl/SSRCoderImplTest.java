package com.bzsoft.ssr.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.bzsoft.ssr.SSRCoder;
import com.bzsoft.ssr.SSRException;

public class SSRCoderImplTest {

	private static BufferedImage loadImage(final InputStream is){
		try {
			return ImageIO.read(is);
		} catch (final IOException e) {
			return null;
		}
	}

	public static void main(final String[] args) throws IOException, SSRException {
		final String s = "This is rock'n'roll radio, stay tuned for more rock'n'roll.";
		{
			final BufferedImage img = loadImage(SSRCoderImplTest.class.getClassLoader().getResourceAsStream("test.png"));
			final SSRCoder c = new SSRCoderImpl();
			final BufferedImage out = c.encode(img, s.getBytes());
			ImageIO.write(out, "png", new File("D:\\testout.png"));
			final byte[] msg = c.decode(out);
			System.out.println(new String(msg));
		}
	}

}
