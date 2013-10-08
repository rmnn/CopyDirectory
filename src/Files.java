import java.io.File;
import java.io.IOException;

/*
 * @author ageevd
 * 
 */

public class Files {

	/*
	 * this method copies directories with their children (files and folders) or files
	 * without replacing existing files
	 * 
	 * @param File sourceLocation - source location, and File targetLocation - target location. Both of this should be directories
	 * @param File targetLocation - target location. Both of this should be directories
	 * 
	 */

	public static void copy(File sourceLocation, File targetLocation)
			throws IOException {	

		java.nio.file.Files.copy(sourceLocation.toPath(),
				targetLocation.toPath());
		if (sourceLocation.isDirectory()) {
			String[] children = sourceLocation.list();
			for (String s : children) {
				copy(new File(sourceLocation, s), new File(targetLocation, s));
			}
		}
	}
}
