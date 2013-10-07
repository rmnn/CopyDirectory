import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

public class CopyDirectoryTest {

	@Test
	public void copyTest() {

		File source = new File("/Directory1");
		File target = new File("/Directory2");

		try {
			createNotSimpleDirectory();
			Directory.copy(source, target);
			assertTrue(compareTwoDirectories(source, target));
			deleteDirectory(source);
			deleteDirectory(target);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	private void createNotSimpleDirectory() throws IOException {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 1; k++) {
					File file = new File("/Directory1/sub" + i + "/sub-sub" + j
							+ "/sub-sub-sub" + k);
					file.mkdirs();
					createSomeFiles(new File("/Directory1/"));
					createSomeFiles(new File("/Directory1/sub" + i));
					createSomeFiles(new File("/Directory1/sub" + i + "/sub-sub"
							+ j));
					createSomeFiles(new File("/Directory1/sub" + i + "/sub-sub"
							+ j + "/sub-sub-sub" + k));
				}
			}
		}
	}

	private void createSomeFiles(File file) throws IOException {
		Random rn = new Random();
		int count = rn.nextInt() % 15;
		for (int i = 0; i <= count; i++) {
			File file1 = new File(file.getAbsolutePath() + "/file" + i);
			file1.createNewFile();
			OutputStream out = new FileOutputStream(file1);
			byte[] buf = new byte[1024];
			Arrays.fill(buf, (byte) 0);
			for (int j = 0; j <= 10; j++) {
				out.write(buf, 0, j);
				buf[Math.abs(rn.nextInt() % 1024)] = (byte) (rn.nextInt() % 128);
			}
			out.close();
		}

	}

	private boolean compareTwoDirectories(File file1, File file2)
			throws Exception {

		if (file1.isDirectory()) {

			String[] children1 = file1.list();
			String[] children2 = file2.list();
			if (children1.length != children2.length) {
				return false;
			}
			for (int i = 0; i < children1.length; i++) {
				if (!compareTwoDirectories(new File(file1, children1[i]),
						new File(file2, children1[i]))) { // children1 here too
															// because file1 and
															// file2 should have
															// the same child
					return false;
				}
			}
		} else {

			if (!compare(file1, file2)) {
				return false;
			}
		}
		return true;
	}

	private boolean compare(File f1, File f2) throws Exception {
		if (f1.length() != f2.length()) {
			return false;
		}
		boolean res = true;
		FileReader readerF1 = new FileReader(f1);
		FileReader readerF2 = new FileReader(f2);
		int byteA;
		int byteB;
		while ((byteA = readerF1.read()) > 0) {
			byteB = readerF2.read();
			if (byteA != byteB) {
				res = false;
				break;
			}
		}
		readerF1.close();
		readerF2.close();
		return res;
	}

	private void deleteDirectory(File file) {
		if (!file.exists())
			return;
		if (file.isDirectory()) {
			for (File f : file.listFiles())
				deleteDirectory(f);
			file.delete();
		} else {
			file.delete();
		}
	}
}
