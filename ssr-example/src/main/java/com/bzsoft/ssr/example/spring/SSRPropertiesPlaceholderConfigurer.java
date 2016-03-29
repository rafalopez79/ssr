package com.bzsoft.ssr.example.spring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

import com.bzsoft.ssr.SSRCoder;
import com.bzsoft.ssr.impl.SSRCoderImpl;

public class SSRPropertiesPlaceholderConfigurer extends PropertyPlaceholderConfigurer{

	private static final String XML_FILE_EXTENSION = ".xml";
	private static final String PNG_FILE_EXTENSION = ".png";

	protected SSRCoder coder = new SSRCoderImpl();

	protected Resource[] locations;
	protected String fileEncoding;
	protected boolean ignoreResourceNotFound;
	protected PropertiesPersister propertiesPersister;

	@Override
	public void setPropertiesPersister(final PropertiesPersister propertiesPersister) {
		this.propertiesPersister =propertiesPersister != null ? propertiesPersister : new DefaultPropertiesPersister();
		super.setPropertiesPersister(propertiesPersister);
	}

	@Override
	public void setLocation(final Resource location) {
		super.setLocation(location);
		this.locations = new Resource[] {location};
	}

	@Override
	public void setLocations(final Resource[] locations) {
		super.setLocations(locations);
		this.locations = locations;
	}

	@Override
	public void setFileEncoding(final String encoding) {
		super.setFileEncoding(encoding);
		this.fileEncoding = encoding;
	}

	@Override
	public void setIgnoreResourceNotFound(final boolean ignoreResourceNotFound) {
		super.setIgnoreResourceNotFound(ignoreResourceNotFound);
		super.setIgnoreResourceNotFound(ignoreResourceNotFound);
	}

	@Override
	protected void loadProperties(final Properties props) throws IOException {
		if (this.locations != null) {
			for (final Resource location : locations) {
				if (logger.isInfoEnabled()) {
					logger.info("Loading properties file from " + location);
				}
				try {
					fillProperties(
							props, new EncodedResource(location, this.fileEncoding), this.propertiesPersister);
				}
				catch (final IOException ex) {
					if (this.ignoreResourceNotFound) {
						if (logger.isWarnEnabled()) {
							logger.warn("Could not load properties from " + location + ": " + ex.getMessage());
						}
					}
					else {
						throw ex;
					}
				}
			}
		}
	}

	protected void fillProperties(final Properties props, final EncodedResource resource, final PropertiesPersister persister)
			throws IOException {

		InputStream stream = null;
		Reader reader = null;
		try {
			final String filename = resource.getResource().getFilename();
			if (filename != null && filename.endsWith(XML_FILE_EXTENSION)) {
				stream = resource.getInputStream();
				persister.loadFromXml(props, stream);
			}else if (filename != null && filename.endsWith(PNG_FILE_EXTENSION)) {
				stream = resource.getInputStream();
				loadFromPNG(coder, props, stream);
			}else if (resource.requiresReader()) {
				reader = resource.getReader();
				persister.load(props, reader);
			}
			else {
				stream = resource.getInputStream();
				persister.load(props, stream);
			}
		}
		finally {
			if (stream != null) {
				stream.close();
			}
			if (reader != null) {
				reader.close();
			}
		}
	}

	protected static void loadFromPNG(final SSRCoder coder, final Properties prop, final InputStream is) throws IOException{
		InputStream aux = null;
		try{
			final byte[] barray = coder.decode(ImageIO.read(is));
			aux = new GZIPInputStream(new ByteArrayInputStream(barray));
			prop.load(aux);
		}catch(final Exception e){
			throw new IOException(e);
		}finally{
			if (aux != null){
				aux.close();
			}
		}
	}

}
