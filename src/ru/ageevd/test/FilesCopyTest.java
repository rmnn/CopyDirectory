package ru.ageevd.test;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import ru.ageevd.copy.FilesCopyManager;
import ru.ageevd.plugins.AntivirusPlugin;
import ru.ageevd.plugins.AntivirusProperties;
import ru.ageevd.plugins.FileCopyPlugin;

public class FilesCopyTest {
	private final OutputStream os = new ByteArrayOutputStream();
	private final PrintStream ps = new PrintStream(os);
	private final Path sourceDirectory = Paths.get("source");
	private final Path targetDirectory = Paths.get("target");
	private Path sourceFile;
	private Path targetFile;
	private FileOutputStream f;

	@Test
	public void copyFileTest() {

		System.setErr(ps);

		try {
			sourceFile = Files.createTempFile("testFile1", ".tmp");
			targetFile = Files.createTempFile("testFile2", ".tmp");

			// lets try to copy non-existent file
			deleteFile(sourceFile);
			FilesCopyManager.copy(sourceFile, targetFile, true, true, false,
					null);

			// we should get a message
			assertTrue(os.toString().contains("does not exist"));

			sourceFile = Files.createTempFile("testFile1", ".tmp");
			writeSomeStuffInFile(sourceFile);

			FilesCopyManager.copy(sourceFile, targetFile, true, true, false,
					null);
			assertTrue(compareTwoFiles(sourceFile, targetFile));

			f = new FileOutputStream(sourceFile.toFile());
			f.write(getInfectedSequence());

			List<FileCopyPlugin> plugins = new ArrayList<FileCopyPlugin>();
			plugins.add(new AntivirusPlugin());

			FilesCopyManager.copy(sourceFile, targetFile, true, true, false,
					plugins);

			// check if we've got message and did'nt copy file
			assertTrue(os.toString().contains("is infected"));
			assertFalse(Arrays.equals(Files.readAllBytes(sourceFile),
					Files.readAllBytes(targetFile)));

		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			closeStream(f);
			deleteFile(sourceFile);
			deleteFile(targetFile);
		}

	}

	@Test
	public void copyFileTreeTest() {
		System.setErr(ps);

		try {
			createNotSimpleFileTree();

			FilesCopyManager.copy(sourceDirectory, targetDirectory, true, true,
					true, null);
			compareTwoFileTrees(sourceDirectory, targetDirectory);

			// it means that we got no mistakes
			assertEquals(os.toString(), "");

			// let's add 'virus'
			Path virus = sourceDirectory.resolve("virus");
			Files.createFile(virus);
			f = new FileOutputStream(virus.toFile());
			f.write(getInfectedSequence());
			List<FileCopyPlugin> plugins = new ArrayList<FileCopyPlugin>();
			plugins.add(new AntivirusPlugin());
			FilesCopyManager.copy(sourceDirectory, targetDirectory, true, true,
					true, plugins);

			// we should get a message
			assertTrue(os.toString().contains("is infected"));

			// let's try to copy directory to a file
			targetFile = Files.createTempFile("testFile1", ".tmp");

			FilesCopyManager.copy(sourceDirectory, targetFile, true, true,
					true, null);

			// we should get a message again
			assertTrue(os.toString()
					.contains("Cant copy directory to the file"));

		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			closeStream(f);
			deleteFile(sourceFile);
			deleteFileTree(sourceDirectory);
			deleteFileTree(targetDirectory);
		}

	}

	private void deleteFile(Path file) {
		try {
			if (file != null) {
				Files.delete(file);
			}
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	private void closeStream(Closeable s) {
		try {
			if (s != null)
				s.close();
		} catch (IOException e) {
			fail(e.getMessage());
		}

	}

	private void createNotSimpleFileTree() throws IOException {
		final String sub = "sub";
		final String sep = File.separator;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 1; k++) {
					Path dir = Paths.get(sourceDirectory + sep + sub + i + sep
							+ sub + j + sep + sub + k);

					Files.createDirectories(dir);
					createSomeFiles(dir);

				}
			}
		}
	}

	private void createSomeFiles(Path file) throws IOException {
		for (int i = 0; i <= 10; i++) {
			Path pth = file.resolve("file" + i);
			Files.createFile(pth);
			writeSomeStuffInFile(pth);
		}

	}

	private void deleteFileTree(Path start) {
		try {
			Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir,
						IOException e) throws IOException {
					if (e == null) {
						Files.delete(dir);
						return CONTINUE;
					} else {
						// directory iteration failed
						throw e;
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void compareTwoFileTrees(final Path source, final Path target) {
		try {

			Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) {
					Path relativePath = target.resolve(source.relativize(dir));
					if (!Files.exists(relativePath)) {
						System.err.format(
								"%s : cant find dir in target location",
								relativePath);
						return TERMINATE;
					}

					return CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {

					Path relativeFile = target.resolve(source.relativize(file));
					if (!compareTwoFiles(file, relativeFile)) {
						System.err.format(
								"%s : cant find file or they are not equals",
								relativeFile);
						return TERMINATE;
					}
					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir,
						IOException e) throws IOException {
					if (e == null) {

						return CONTINUE;
					} else {
						// directory iteration failed
						throw e;
					}
				}
			});
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	private boolean compareTwoFiles(Path file1, Path file2) throws IOException {
		return Arrays.equals(Files.readAllBytes(file1),
				Files.readAllBytes(file2));
	}

	private void writeSomeStuffInFile(Path file) throws IOException {
		Random rnd = new Random();
		byte[] bytes = new byte[50];
		rnd.nextBytes(bytes);
		Files.write(file, bytes);
	}

	private byte[] getInfectedSequence() {
		List<byte[]> bytesList = AntivirusProperties.getByteSequences();
		if (bytesList.size() == 0) {
			fail("You should have at least one sequence in file antivirus.properties. Please write one");
		}
		return bytesList.get(0);
	}

}
