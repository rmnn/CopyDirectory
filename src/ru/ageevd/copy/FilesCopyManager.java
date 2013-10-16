package ru.ageevd.copy;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.EnumSet;

import ru.ageevd.plugins.FileCopyPlugin;

/**
 * This class has only one static method which can copy files or directories.
 * 
 */

public class FilesCopyManager {

	/**
	 * Copy source file of directory to target location. If {@code prompt} is
	 * true then prompt user to overwrite target if it exists. The
	 * {@code preserve} parameter determines if file attributes should be
	 * copied/preserved.
	 */
	public static void copy(Path source, Path target, boolean prompt,
			boolean preserve, boolean recursive,
			Collection<FileCopyPlugin> plugins) throws IOException {

		// check if source is exist
		if (!Files.exists(source)) {
			System.err.format("%s : does not exist", source);
			return;
		}

		// check if target is a directory
		boolean isDir = Files.isDirectory(target);

		// check if target is a file and source is a directory
		if (Files.exists(target) && !isDir && Files.isDirectory(source)) {
			System.err.println("Cant copy directory to the file");
			return;
		}

		Path dest = (isDir) ? target.resolve(source.getFileName()) : target;

		if (recursive) {
			// follow links when copying files
			EnumSet<FileVisitOption> opts = EnumSet
					.of(FileVisitOption.FOLLOW_LINKS);
			TreeCopier tc = new TreeCopier(source, dest, prompt, preserve,
					plugins);
			Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
		} else {
			// not recursive so source must not be a directory
			if (Files.isDirectory(source)) {
				System.err.format("%s: is a directory%n", source);
			}
			copyFile(source, dest, prompt, preserve, plugins);
		}
	}

	private static void copyFile(Path source, Path target, boolean prompt,
			boolean preserve, Collection<FileCopyPlugin> plugins) {

		CopyOption[] options = (preserve) ? new CopyOption[] { COPY_ATTRIBUTES,
				REPLACE_EXISTING } : new CopyOption[] { REPLACE_EXISTING };
		if (prompt || Files.notExists(target)) {
			try {
				if (plugins != null) {
					// check if we can copy file
					for (FileCopyPlugin plugin : plugins) {
						if (!plugin.canCopyFile(source)) {
							System.err.format(
									"Unable to copy: %s: the file is infected",
									source);
							return;
						}
					}
				}
				Files.copy(source, target, options);
			} catch (IOException x) {
				System.err.format("Unable to copy: %s: %s%n", source, x);
			}
		}
	}

	/**
	 * A {@code FileVisitor} that copies a file-tree
	 */
	static class TreeCopier implements FileVisitor<Path> {
		private final Path source;
		private final Path target;
		private final boolean prompt;
		private final boolean preserve;
		private final Collection<FileCopyPlugin> plugins;

		TreeCopier(Path source, Path target, boolean prompt, boolean preserve,
				Collection<FileCopyPlugin> plugins) {
			this.source = source;
			this.target = target;
			this.prompt = prompt;
			this.preserve = preserve;
			this.plugins = plugins;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir,
				BasicFileAttributes attrs) {
			// before visiting entries in a directory we copy the directory
			// (okay if directory already exists).
			CopyOption[] options = (preserve) ? new CopyOption[] { COPY_ATTRIBUTES }
					: new CopyOption[0];

			Path newdir = target.resolve(source.relativize(dir));
			try {
				Files.copy(dir, newdir, options);
			} catch (FileAlreadyExistsException x) {
				// ignore
			} catch (IOException x) {
				System.err.format("Unable to create: %s: %s%n", newdir, x);
				return SKIP_SUBTREE;
			}
			return CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			copyFile(file, target.resolve(source.relativize(file)), prompt,
					preserve, plugins);
			return CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
			// fix up modification time of directory when done
			if (exc == null && preserve) {
				Path newdir = target.resolve(source.relativize(dir));
				try {
					FileTime time = Files.getLastModifiedTime(dir);
					Files.setLastModifiedTime(newdir, time);
				} catch (IOException x) {
					System.err.format(
							"Unable to copy all attributes to: %s: %s%n",
							newdir, x);
				}
			}
			return CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			if (exc instanceof FileSystemLoopException) {
				System.err.println("cycle detected: " + file);
			} else {
				System.err.format("Unable to copy: %s: %s%n", file, exc);
			}
			return CONTINUE;
		}
	}

}
