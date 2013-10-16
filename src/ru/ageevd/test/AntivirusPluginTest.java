package ru.ageevd.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import ru.ageevd.plugins.AntivirusPlugin;
import ru.ageevd.plugins.AntivirusProperties;

public class AntivirusPluginTest {

	@Test
	public void test() {

		AntivirusPlugin antivirusPlugin = new AntivirusPlugin();
		Path testFile = null;
		FileOutputStream f = null;

		try {

			List<byte[]> bytesList = AntivirusProperties.getByteSequences();
			if (bytesList.size() == 0) {
				fail("You should have at least one sequence in file antivirus.properties. Please write one");
			}

			// empty file can't be infected
			testFile = Files.createFile(Paths.get("testfile"));
			assertTrue(antivirusPlugin.canCopyFile(testFile));

			// adding infected sequence of bytes to file
			f = new FileOutputStream(testFile.toFile());
			f.write(bytesList.get(0));

			assertFalse(antivirusPlugin.canCopyFile(testFile));

		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			closeStream(f);
			deleteFile(testFile);
		}
	}

	private void closeStream(Closeable s) {
		try {
			if (s != null)
				s.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deleteFile(Path file) {
		try {
			if (file != null) {
				Files.delete(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
