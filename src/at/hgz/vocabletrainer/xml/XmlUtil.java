package at.hgz.vocabletrainer.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import at.hgz.vocabletrainer.db.Dictionary;

public class XmlUtil {
	
	private static XmlUtil instance;
	
	public static XmlUtil getInstance() {
		if (instance == null) {
			instance = new XmlUtil();
		}
		return instance;
	}

	public byte[] marshall(Dictionary dictionary) {
		try {
			Serializer serializer = new Persister(new Format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
			XmlDictionary xmlDictionary = null; // TODO
	
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.write(xmlDictionary, out);
			return out.toByteArray();
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public Dictionary unmarshall(byte[] dictionary) {
		try {
			Serializer serializer = new Persister();
			ByteArrayInputStream in = new ByteArrayInputStream(dictionary);
			XmlDictionary xmlDictionary = serializer.read(XmlDictionary.class, in);
			Dictionary ret = null; // TODO
			return ret;
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}
