package ru.ageevd.plugins;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 
 * This class is just for load properties from file named antivirus.properties
 * and get sequences of bytes from this file
 * 
 */
public class AntivirusProperties {
	private static final Properties properties = new Properties();
	static {
		loadProperties(properties, "antivirus.properties");
	}

	private static void loadProperties(Properties result, String resource) {
		FileInputStream f = null;
		try {
			f = new FileInputStream(resource);
			properties.load(f);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeStream(f);
		}
	}

	private static void closeStream(Closeable s) {
		try {
			if (s != null)
				s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<byte[]> getByteSequences() {
		Collection<Object> coll = properties.values();
		List<byte[]> bytesList = new ArrayList<byte[]>();
		for (Object obj : coll) {
			bytesList.add(getBytesFromString((String) obj));
		}
		return bytesList;
	}

	private static byte[] getBytesFromString(String s) {
		String[] strings = s.split(" ");
		byte[] bytes = new byte[strings.length];
		for (int i = 0; i < strings.length; i++) {
			bytes[i] = Byte.parseByte(strings[i], 16);
		}
		return bytes;

	}

}
