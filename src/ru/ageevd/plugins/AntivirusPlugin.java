package ru.ageevd.plugins;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * This class implements {@link FileCopyPlugin} interface.
 * 
 */
public class AntivirusPlugin implements FileCopyPlugin {

	/**
	 * This method return true if file does not have sequences of bytes that we
	 * specified in the properties file named antivirus.properties, false
	 * otherwise
	 * 
	 * @throws IOException when can't read bytes from file
	 * 
	 */

	@Override
	public boolean canCopyFile(Path file) throws IOException {

		byte[] bytes = Files.readAllBytes(file);
		List<byte[]> bytesList = AntivirusProperties.getByteSequences();
		for (byte[] pattern : bytesList) {
			if (KMPMatch.indexOf(bytes, pattern) != -1) {
				return false;
			}
		}
		return true;
	}

}
