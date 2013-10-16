package ru.ageevd.plugins;

import java.io.IOException;
import java.nio.file.Path;

/**
 * An object that has only one method which says can we copy file or not.
 * 
 */

public interface FileCopyPlugin {

	/**
	 * This method return true if we can copy file,
	 * false otherwise  
	 * 
	 *  @throws IOException, it depends on implementation of this interface 
	 *  
	 */
	
	public boolean canCopyFile(Path file) throws IOException;

}
