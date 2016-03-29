package com.bzsoft.ssr.plugin;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;

import com.bzsoft.ssr.SSRCoder;
import com.bzsoft.ssr.impl.SSRCoderImpl;

@Mojo(name="ssr")
public class SSRMojo extends AbstractMojo {

	private final SSRCoder coder;

	@Parameter
	private File inputFile;
	@Parameter
	private File outputFile;
	@Parameter
	private File[] files;

	public SSRMojo() {
		coder = new SSRCoderImpl();
	}

	@Override
	public void execute() throws MojoExecutionException {
		getLog().info("Running SSR ");
		getLog().info(" - input file:  "+inputFile);
		getLog().info(" - output file: "+outputFile);
		getLog().info(" - properties:  "+Arrays.toString(files));
		if (inputFile == null || outputFile == null){
			getLog().info("nor input file nor output file can be null");
			throw new MojoExecutionException("nor input file nor output file can be null");
		}
		getLog().info("Context:"+getPluginContext());
		if (files == null || files.length == 0){
			copyFiles(inputFile, outputFile);
		}else{
			final List<File> list = getFiles(files);
			final byte[] data = getData(list);
			getLog().info("Processing "+data.length+" bytes ...");
			InputStream is = null;
			try{
				is = new FileInputStream(inputFile);
				final BufferedImage img = coder.encode(loadImage(is), data);
				ImageIO.write(img, "png", outputFile);
			}catch(final Exception e){
				throw new MojoExecutionException(e.getMessage(), e);
			}finally{
				IOUtil.close(is);
			}
		}
	}

	private static BufferedImage loadImage(final InputStream is) throws IOException{
		return ImageIO.read(is);
	}

	private final static List<File> getFiles(final File[] fileNames){
		final List<File> list = Arrays.asList(fileNames);
		return list;
	}

	private static final byte[] getData(final List<File> files) throws MojoExecutionException{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzos = null;
		BufferedReader r = null;
		try{
			gzos = new GZIPOutputStream(baos);
			for(final File f : files){
				r = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				String line = null;
				while((line = r.readLine())!= null){
					line = line.trim();
					if (!line.startsWith("#")){
						gzos.write(line.getBytes());
						gzos.write('\n');
					}
				}
				r.close();
			}

		}catch(final IOException e){
			throw new MojoExecutionException(e.getMessage(), e);
		}finally{
			IOUtil.close(r);
			IOUtil.close(gzos);
		}
		return baos.toByteArray();
	}

	private static final void copyFiles(final File input, final File output)throws MojoExecutionException{
		final int bufferSize = 10*1024;
		InputStream is = null;
		OutputStream os = null;
		try{
			is = new FileInputStream(input);
			os = new FileOutputStream(output);
			IOUtil.copy(is, os, bufferSize);
		}catch(final IOException e){
			throw new MojoExecutionException(e.getMessage(), e);
		}finally{
			IOUtil.close(is);
			IOUtil.close(os);
		}
	}

}
