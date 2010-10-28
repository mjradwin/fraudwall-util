/**
 * Copyright (c) 2010, Anchor Intelligence. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 *
 * - Neither the name of Anchor Intelligence nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.fraudwall.util.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fraudwall.util.FWProps;
import com.fraudwall.util.Utilities;
import com.fraudwall.util.exc.AnchorFatalError;
import com.fraudwall.util.exc.ArgCheck;

/**
 * Defines various static I/O utility methods.
 */
public abstract class IOUtils {

	private static final Log log = LogFactory.getLog(IOUtils.class);

	/** The temporary directory to use on Unix/Mac systems (see bug 2741). */
	private static final String UNIX_TMP_DIR = "/var/tmp";

	/**
	 * Name of the property that specifies the OS specific temp directory, e.g /tmp on unix
	 */
	public static final String TMP_DIR_PROP_NAME = "java.io.tmpdir";

	/**
	 * Cached UTF-8 character set.
	 */
	public static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * The newline string as defined by the "line.separator" property. Defaults
	 * to a single newline character ("\n") if that property is not defined.
	 */
	public static final String NEW_LINE = System.getProperty("line.separator", "\n");

	/**
	 * Creates a new {@link File} with the name <code>fileName</code> in the
	 * directory <code>dirName</code>. If the directory does not already
	 * exist on the file system, it is created. No file is created on the file
	 * system by this method.
	 *
	 * @param dirName
	 *            The name of the directory in which to create the file.
	 * @param fileName
	 *            The name used in the resulting {@link File}.
	 * @return The newly-created {@link File}.
	 */
	public static File newFile(String dirName, String fileName) throws IOException {
		createDirectoryIfNeeded(new File(dirName));
		File file = new File(dirName, fileName);
		return file;
	}

	/**
	 * Ensures that <code>dir</code> is a directory, creating it and any
	 * ancestor directories to it as necessary.
	 *
	 * @param dir
	 *            The directory to create.
	 * @throws IOException
	 *             If <code>dir</code> exists but is not a directory, or if
	 *             <code>dir</code> cannot be created.
	 */
	public static void createDirectoryIfNeeded(File dir) throws IOException {
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new IOException("Can't create directory " + dir);
			}
		} else if (!dir.isDirectory()) {
			throw new IOException(dir + " already exists, but is not a directory");
		}
	}

	/**
	 * Attempts to find a directory that is an ancestor of the current directory
	 * and that contains both "src" and "test" sub-directories.
	 *
	 * @return absolute path of directory, or {@code null} if it could not be found.
	 */
	public static String locateRootDirectory() {
		final File[] fsRoots = File.listRoots(); // stop when we get to the root
		try {
			File dir = new File(new File(".").getCanonicalPath());
			while (true) {
				if (new File(dir, "src").exists() && new File(dir, "test").exists()) {
					return dir.getAbsolutePath();
				}
				// Are we at the root?
				for (File fsRoot : fsRoots) {
					if (fsRoot.equals(dir)) {
						return null;
					}
				}
				dir = dir.getParentFile();
			}
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Assuming <code>inFile</code> names an existing gzip-compressed file whose
	 * name ends with ".gz", creates a new, uncompressed version of the file in
	 * the same directory with the same name, but with the ".gz" extension removed.
	 * If a file by that name already exists in that directory, this method is a
	 * no-op.
	 *
	 * @return a {@link File} handle on the uncompressed output file.
	 *
	 * @throws IOException If there are errors reading the compressed input file
	 * or creating/writing the uncompressed output file.
	 */
	public static File gunzip(File inFile) throws IOException {
		return gunzip(inFile, new File(inFile.getParent(), Utilities.chopLastComponent(inFile.getName())));
	}

	/**
	 * Assuming <code>inFile</code> names an existing gzip-compressed file whose
	 * name ends with ".gz", creates a new, uncompressed version of the file in
	 * <code>outFile</code>.
	 * If a file by that name already exists in that directory, this method is a
	 * no-op.
	 *
	 * @return a {@link File} handle on the uncompressed output file.
	 *
	 * @throws IOException If there are errors reading the compressed input file
	 * or creating/writing the uncompressed output file.
	 */
	public static File gunzip(File inFile, File outFile) throws IOException {
		String inName = inFile.getName();
		ArgCheck.isTrue(inName.endsWith(".gz"), "input file does not end with .gz extension");
		if (!outFile.exists()) {
			InputStream inStream = getInputStream(inFile);
			OutputStream outStream = null;
			try {
				outStream = new FileOutputStream(outFile);
				byte[] buf = new byte[1024 * 64];
				int numRead;
				while ((numRead = inStream.read(buf)) >= 0) {
					outStream.write(buf, 0, numRead);
				}
			} finally {
				if (inStream != null) inStream.close();
				if (outStream != null) {
					outStream.flush();
					outStream.close();
				}
			}
		}
		return outFile;
	}

	/**
	 * Returns "/var/tmp", except on Windows, in which case returns the
	 * temporary directory specified by the System property named by
	 * {@link #TMP_DIR_PROP_NAME}.
	 * <p>
	 * This method should be used with care, since multiple processes
	 * running on the same host may attempt to incorrectly write to or
	 * access the same file if the names of files created in this directory
	 * are not process-specific.
	 *
	 * @see #getRootTmpDir()
	 */
	public static File getGlobalTmpDir() {
		return Utilities.isWindowsOS()
			? new File(System.getProperty(TMP_DIR_PROP_NAME))
			: new File(UNIX_TMP_DIR);
	}

	/**
	 * Returns the ROOT-specific temporary directory.
	 *
	 * @see #getGlobalTmpDir()
	 */
	public static File getRootTmpDir() {
		return new File(getRootDir(), "tmp");
	}

	/**
	 * Returns the directory that contains generated
	 * SQL files that is specific to this $ROOT (or
	 * when called from a unit test, this source tree)
	 */
	public static File getGeneratedSqlDir() {
		return Utilities.isCalledFromUnitTest()
			? new File(IOUtils.locateRootDirectory(), "generated/sql")
			: new File(IOUtils.getRootDir(), "sql/generated");
	}

	/**
	 * Returns a File object in the ROOT tmpdir.
	 */
	public static File getAppLockfile(String lockName) {
		return new File(getGlobalTmpDir(), lockName + ".lock");
	}

	/**
	 * Truncates files older than the specified age limit in the specified directory.
	 * @param directory directory in which files will be truncated
	 * @param minModificationTime any file whose last modified time is < this value may be truncated
	 * @param pattern optional regular expression that file names must match to be truncated
	 * @param recursive if true, then files in sub directories will be truncated
	 * @throws IOException
	 */
	public static void truncateOldFiles(File directory, long minModificationTime, String pattern, boolean recursive)
		throws IOException {
		cleanOldFiles(directory, minModificationTime, pattern, recursive, false);
	}

	/**
	 * Deletes files older than the specified age limit in the specified directory.
	 * @param directory directory in which files will be deleted
	 * @param minModificationTime any file whose last modified time is < this value may be deleted
	 * @param pattern optional regular expression that file names must match to be deleted
	 * @param recursive if true, then files in sub directories will be deleted
	 * @throws IOException
	 */
	public static void deleteOldFiles(File directory, long minModificationTime, String pattern, boolean recursive)
		throws IOException {
		cleanOldFiles(directory, minModificationTime, pattern, recursive, true);
	}

	private static void cleanOldFiles(File directory, long minModificationTime, String pattern, boolean recursive, boolean delete)
		throws IOException {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				if (recursive) {
					cleanOldFiles(file, minModificationTime, pattern, recursive, delete);
				}
			} else {
				if (pattern == null || file.getName().matches(pattern)) {
					if (file.lastModified() < minModificationTime) {
						if (delete) {
							log.info("deleting " + file);
							file.delete();
						} else {
							if (file.length() > 0) {
								log.info("truncating " + file);
								new FileWriter(file).close();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the name of the directory specified by the property
	 * {@link FWProps#ROOT_PROP_NAME}, which defaults to the value of the
	 * environment variable <code>ROOT</code> or
	 * <code>/var/fraudwall</code> if unset.
	 */
	public static String getRootDir() {
		return FWProps.getProperty(FWProps.ROOT_PROP_NAME);
	}

	/**
	 * Deletes the directory tree rooted at <code>dir</code> and all files
	 * contained in it. Throws an {@link IllegalArgumentException} if any of the files or
	 * the directory itself cannot be deleted (e.g., because there is an open
	 * file handle to that file system entity).
	 * <p>
	 * This method fails with an assertion error if the directory
	 * <code>rootDir</code> does not exist or is not a directory.
	 */
	public static void deleteDirectoryTree(File dir) {
		ArgCheck.isTrue(dir.exists(), dir.getPath() + " does not exist");
		ArgCheck.isTrue(dir.isDirectory(), dir.getPath() + " is not a directory");
		for (File fd: dir.listFiles()) {
			if (fd.isDirectory()) {
				deleteDirectoryTree(fd);
			} else {
				deleteFileOrDir(fd);
			}
		}
		deleteFileOrDir(dir);
	}

	/**
	 * Deletes all files or directories in the directory tree rooted at
	 * <code>dir</code>. Throws an {@link IllegalArgumentException} if any of
	 * the files or the directory itself cannot be deleted (e.g., because there
	 * is an open file handle to that file system entity).
	 * <p>
	 * This method fails with an assertion error if the directory
	 * <code>dir</code> does not exist or is not a directory.
	 */
	public static void deleteAllFilesInDirectory(File dir) {
		ArgCheck.isTrue(dir.exists(), dir.getPath() + " does not exist");
		ArgCheck.isTrue(dir.isDirectory(), dir.getPath() + " is not a directory");
		File[] files = dir.listFiles();
		log.info("Deleting " + files.length + " files from " + dir);
		for (File fd: files) {
			if (fd.isDirectory()) {
				deleteDirectoryTree(fd);
			} else {
				deleteFileOrDir(fd);
			}
		}
	}

	/**
	 * Deletes the directory or file <code>fd</code> from the file
	 * system. Throws a {@link IllegalStateException} if the specified file
	 * system entity cannot be deleted (e.g., because there is an open
	 * file handle to that entity, or because the entity is a non-empty
	 * directory).
	 */
	public static void deleteFileOrDir(File fd) {
		boolean deleted = fd.delete();
		if (!deleted) {
			String type = fd.isFile() ? "file" : "directory";
			throw new IllegalStateException("Unable to delete " + type + " '" + fd.getPath() + "'");
		}
	}

	/**
	 * Returns a sorted list of all the files in <code>directory</code> which end with any of the
	 * specified <code>extensions</code>.  If <code>recurse</code> is true, then files in
	 * subdirectories of <code>directory</code> will be included.
	 */
	@SuppressWarnings({ "cast", "unchecked" })
	public static List<File> listFiles(File directory, boolean recurse, String... extensions) {
		ArrayList<File> files = new ArrayList<File>(((Collection<File>)FileUtils.listFiles(directory, extensions, recurse)));
		Collections.sort(files);
		return files;
	}

	/**
	 * Returns a sorted list of all the files in <code>directory</code> and its
	 * subdirectories which are accepted by <code>filter</code>..
	 */
	public static List<File> listFiles(File directory, FilenameFilter filter) {
		return listFiles(directory, createIOFileFilter(filter));
	}

	/**
	 * Returns a newly created IOFileFilter by wrapping <code>filter</code>.
	 */
	public static IOFileFilter createIOFileFilter(final FilenameFilter filter) {
		return new IOFileFilter() {

			public boolean accept(File file) {
				return filter.accept(file.getParentFile(), file.getName());
			}

			public boolean accept(File dir, String name) {
				return filter.accept(dir, name);
			}

		};
	}

	/**
	 * Returns a sorted list of all the files in <code>directory</code> and its
	 * subdirectories which are accepted by <code>filter</code>..
	 */
	@SuppressWarnings({ "cast", "unchecked" })
	public static List<File> listFiles(File directory, IOFileFilter filter) {
		ArrayList<File> files =
			new ArrayList<File>(((Collection<File>)FileUtils.listFiles(directory, filter, TrueFileFilter.INSTANCE)));
		Collections.sort(files);
		return files;
	}

	/**
	 * Creates a new empty file in the same directory as <code>file</code> named
	 * <code>.file.XXX.tmp</code> similar to rsync's temporary files.  After
	 * data has been written to this file, it can be atomically renamed to
	 * <code>file</code> via {@link IOUtils#renameFile(File, File)}
	 */
	public static File createAtomicRenameFileFor(File file) {
		try {
			return File.createTempFile("." + file.getName() + ".", ".tmp", file.getParentFile());
		} catch (IOException e) {
			throw new AnchorFatalError("Unable to create temporary file for " + file, e);
		}
	}

	/**
	 * Renames <code>source</code> to <code>target</code> using {@link File#renameTo(File)}
	 * and throws an {@link AnchorFatalError} renameTo fails.  If <code>target</code>
	 * already exists, then it will be replaced.
	 */
	public static void renameFile(File source, File target) {
		ArgCheck.isTrue(source.exists(), source + " does not exist");
		ArgCheck.isTrue(target.getParentFile().exists(), target.getParentFile() + " does not exist");
		if (target.exists()) {
			target.delete();
		}
		if (!source.renameTo(target)) {
			throw new AnchorFatalError("Unable to rename " + source + " to " + target);
		}
	}

	/**
	 * Moves every file in <code>files</code> to the existing directory <code>directory</code>.
	 */
	public static void moveFilesToDirectory(File directory, File ... files) throws IOException {
		ArgCheck.isExistingDirectory(directory, "directory");
		log.info("Moving " + files.length + " files to " + directory);
		for (File file : files) {
			FileUtils.moveFileToDirectory(file, directory, /*createDestDir=*/ false);
		}
	}

	/**
	 * Like {@link FileUtils#moveFileToDirectory(File, File, boolean)} but does so using
	 * a copy to an intermediate tmpFile followed by removal of the source.
	 * @throws IOException
	 */
	public static void atomicMoveFileToDirectory(File srcFile, File destDir) throws IOException {
		atomicMoveFile(srcFile, new File(destDir, srcFile.getName()));
	}

	/**
	 * Like {@link FileUtils#moveFile(File, File)} but does so using
	 * a copy to an intermediate tmpFile followed by removal of the source.
	 * @throws IOException
	 */
	public static void atomicMoveFile(File srcFile, File destFile) throws IOException {
		File tmpFile = createAtomicRenameFileFor(destFile);
		FileUtils.copyFile(srcFile, tmpFile);
		renameFile(tmpFile, destFile);
		deleteFileOrDir(srcFile);
	}

	/**
	 * Exception thrown when process-level lock could not be acquired because it
	 * is already held by another process.
	 */
	@SuppressWarnings("serial")
	public static class LockAcquisitionError extends AnchorFatalError {
		public LockAcquisitionError(String errMsg) {
			super(errMsg);
		}
	}

	/**
	 * Creates a new {@link FileLock} on the file <code>lockFile</code> which
	 * can be released by calling {@link #releaseLock(FileLock)}. This lock is
	 * guaranteed to be exclusive to a single process, but it is not guaranteed
	 * to be reentrant.
	 *
	 * @throws LockAcquisitionError in the event that the lock identified
	 * by {@code lockFile} could not be acquired because it is already held by
	 * another process.
	 */
	public static FileLock getLock(File lockFile) {
		try {
			IOUtils.createDirectoryIfNeeded(lockFile.getParentFile());
			FileOutputStream fos = new FileOutputStream(lockFile);
			FileLock fileLock = fos.getChannel().tryLock();
			if (fileLock == null) {
				fos.close();
				if (!Utilities.isCalledFromUnitTest()) {
					log.error("Unable to obtain lock " + lockFile + ". exiting...");
				}
				throw new LockAcquisitionError("Unable to get lock " + lockFile);
			}
			return fileLock;
		} catch (IOException e) {
			log.error("Encountered IOException obtaining lock " + lockFile + ". exiting...");
			throw new AnchorFatalError("Unable to get lock " + lockFile, e);
		}
	}

	/**
	 * Releases the {@link FileLock} obtained by calling {@link #getLock(File)}.
	 */
	public static void releaseLock(FileLock lockToRelease) {
		try {
			if (lockToRelease != null) {
				lockToRelease.release();
				lockToRelease.channel().close();
			}
		} catch (IOException ex) {
			log.error("Failed to release Run lock; exiting...");
			throw new AnchorFatalError("Failed to release Run lock.", ex);
		}
	}

	/**
	 * Generates a warning if <code>line</code> contains the Unicode Replacement Character,
	 * which is indicative of an encoding mismatch.
	 */
	public static void warnAboutUnicodeReplacementChar(String line, String fileName, int lineNo) {
		int unicodeReplacementCharIdx = line.indexOf('\uFFFD');
		if (unicodeReplacementCharIdx != -1) {
			log.warn("Unicode REPLACEMENT CHARACTER detected at "
				+ fileName + " line " + lineNo + " char " + unicodeReplacementCharIdx
				+ ", possible incorrect charset, currently "
				+ getClickLogCharacterEncoding().name());
		}
	}

	/**
	 * Property that controls which character set to use when reading click files from this customer.
	 */
	private static final String CLICK_LOG_CHARACTER_ENCODING_PROP_NAME = "Customer.clickLogCharacterEncoding";

	/**
	 * The default character set to use when reading click log files, typically UTF-8.
	 */
	public /*test*/ static /*final*/ Charset CLICK_LOG_CHARACTER_ENCODING;

	/**
	 * The default character set to use when reading/writing files. Typically
	 * "ISO-8859-1" (also known as ISOLatin1), but may be overridden by the
	 * customer.characterEncoding property.
	 * @see #CLICK_LOG_CHARACTER_ENCODING
	 * @see #CLICK_LOG_CHARACTER_ENCODING_PROP_NAME
	 */
	public static Charset getClickLogCharacterEncoding() {
		if (CLICK_LOG_CHARACTER_ENCODING == null) {
			CLICK_LOG_CHARACTER_ENCODING = Charset.forName(FWProps.getStringProperty(CLICK_LOG_CHARACTER_ENCODING_PROP_NAME));
		}
		return CLICK_LOG_CHARACTER_ENCODING;
	}

	// ------------------------------------------------- reader creation

	/**
	 * Returns a new {@link InputStreamReader} on the given <code>file</code>,
	 * unzipping the file on the fly if the file's name ends with ".gz".
	 *
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a
	 *             regular file, or for some other reason cannot be opened for
	 *             reading.
	 * @throws IOException
	 *             if an I/O error occurs or <code>file</code> has a ".bz2"
	 *             extension.
	 */
	private static InputStream getInputStream(File file) throws FileNotFoundException, IOException {
		InputStream is = new FileInputStream(file);
		return wrapDecompressor(is, file);
	}

	private static InputStream wrapDecompressor(InputStream is, File file) throws IOException {
		if (file.getName().endsWith(".gz")) {
			is = new MultiMemberGZIPInputStream(is, /*bufferSize=*/ 10240);
		} else if (file.toString().endsWith(".bz2")) {
			throw new IOException(".bz2 support disabled");
		}
		return is;
	}

	/**
	 * Returns a new {@link InputStreamReader} on the given <code>file</code>,
	 * unzipping the file on the fly if the file's name ends with ".gz".
	 *
	 * @param charset
	 *            Character set used to do byte-to-character conversion.
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a
	 *             regular file, or for some other reason cannot be opened for
	 *             reading.
	 * @throws IOException
	 *             if an I/O error occurs or <code>file</code> has a ".bz2"
	 *             extension.
	 */
	/*test*/ static InputStreamReader getInputStreamReader(File file, Charset charset)
		throws FileNotFoundException, IOException
	{
		InputStream is = getInputStream(file);
		return new InputStreamReader(is, charset);
	}

	/**
	 * Returns an {@link AnchorLineNumberReader} on the given <code>file</code>,
	 * unzipping the file on the fly if the file's name ends with ".gz". In the
	 * latter case, a {@link MultiThreadedLineNumberReader} is created to do the
	 * on-the-fly decompression in a separate thread. In this case, if you do
	 * not read to the end of file, you need to explicitly
	 * {@link MultiThreadedLineNumberReader#close() close} the reader, or the
	 * background reading thread will remain blocked and your program will not
	 * exit.
	 * <p>
	 * This method uses the character set {@link #UTF8} to do byte to
	 * character conversion.
	 *
	 * @throws IOException
	 *             If there is an error opening the BufferedReader on the given
	 *             file or <code>file</code> has a ".bz2" extension.
	 * @see IOUtils#getLineNumberReader(File, Charset)
	 */
	public static AnchorLineNumberReader getLineNumberReader(File file) throws IOException {
		return IOUtils.getLineNumberReader(file, UTF8);
	}

	/**
	 * Returns an {@link AnchorLineNumberReader} on the given <code>file</code>,
	 * unzipping the file on the fly if the file's name ends with ".gz". In the
	 * latter case, a {@link MultiThreadedLineNumberReader} is created to do the
	 * on-the-fly decompression in a separate thread. In this case, if you do
	 * not read to the end of file, you need to explicitly
	 * {@link MultiThreadedLineNumberReader#close() close} the reader, or the
	 * background reading thread will remain blocked and your program will not
	 * exit.
	 *
	 * @param cs
	 *            Character set to use in converting bytes to characters.
	 * @throws IOException
	 *             If there is an error opening the BufferedReader on the file
	 *             or <code>file</code> has a ".bz2" extension.
	 *
	 * @see #getInputStreamReader
	 */
	public static AnchorLineNumberReader getLineNumberReader(File file, Charset cs) throws IOException {
		boolean useMultiThreaderReader = file.getName().endsWith(".gz");
		return getLineNumberReader(file, cs, useMultiThreaderReader);
	}

	/**
	 * Returns an {@link AnchorLineNumberReader} on the given <code>file</code>,
	 * unzipping the file on the fly if the file's name ends with ".gz".
	 *
	 * @param cs
	 *            Character set to use in converting bytes to characters.
	 * @param useMultiThreadedReader
	 *            If true, return a {@link MultiThreadedLineNumberReader} that
	 *            reads lines from the backing file in a separate thread, which
	 *            is useful if getting lines from the backing file is compute
	 *            intensive, such as when reading from a compressed file and
	 *            decompressing on-the-fly. If false, a regular
	 *            {@link AnchorLineNumberReader} is returned. If using a
	 *            multi-threaded reader and you do not read to the end of file,
	 *            you need to explicitly
	 *            {@link MultiThreadedLineNumberReader#close() close} the
	 *            reader, or the background reading thread will remain blocked
	 *            and your program will not exit.
	 * @throws IOException
	 *             If there is an error opening the BufferedReader on the file
	 *             or <code>file</code> has a ".bz2" extension.
	 *
	 * @see #getInputStreamReader
	 */
	public static AnchorLineNumberReader getLineNumberReader(File file, Charset cs, boolean useMultiThreadedReader)
		throws FileNotFoundException, IOException
	{
		Reader r = getInputStreamReader(file, cs);
		return getLineNumberReader(r, useMultiThreadedReader);
	}

	/**
	 * Creates and returns a new {@link AnchorLineNumberReader} on the given
	 * stream, unzipping the file on the fly if the given <code>file</code>'s
	 * name ends with ".gz". The stream <code>is</code> is expected to have
	 * been created on the given <code>file</code>.. If <code>file</code>'s
	 * name has a ".gz" extension, the returned result will actually be a
	 * {@link MultiThreadedLineNumberReader}, which forks a separate thread to
	 * read lines from the file (thereby doing the on-the-fly decompression work
	 * in a separate thread). In this case, if you do not read to the end of
	 * file, you need to explicitly
	 * {@link MultiThreadedLineNumberReader#close() close} the reader, or the
	 * background reading thread will remain blocked and your program will not
	 * exit.
	 * <p>
	 * This method uses the character set {@link #UTF8} to do byte to
	 * character conversion.
	 *
	 * @throws IOException
	 *             If there is an error opening the reader on the given stream.
	 */
	public static AnchorLineNumberReader getLineNumberReader(InputStream is, File file) throws IOException {
		boolean useMultiThreadedReader = file.getName().endsWith(".gz");
		return getLineNumberReader(is, file, useMultiThreadedReader);
	}

	/**
	 * Creates and returns a new {@link AnchorLineNumberReader} on the given
	 * stream, unzipping the file on the fly if the given <code>file</code>'s
	 * name ends with ".gz". The stream <code>is</code> is expected to have been created on the
	 * given <code>file</code>.<p>
	 *
	 * This method uses the character set {@link #UTF8} to do byte
	 * to character conversion.
	 *
	 * @param useMultiThreadedReader
	 *            If true, return a {@link MultiThreadedLineNumberReader} that
	 *            reads lines from the backing file in a separate thread, which
	 *            is useful if getting lines from the backing file is compute
	 *            intensive, such as when reading from a compressed file and
	 *            decompressing on-the-fly. If false, a regular
	 *            {@link AnchorLineNumberReader} is returned. If using a
	 *            multi-threaded reader and you do not read to the end of file,
	 *            you need to explicitly
	 *            {@link MultiThreadedLineNumberReader#close() close} the
	 *            reader, or the background reading thread will remain blocked,
	 *            and your program will not exit.
	 * @throws IOException
	 *             If there is an error opening the reader on the given stream.
	 */
	public static AnchorLineNumberReader getLineNumberReader(InputStream is, File file, boolean useMultiThreadedReader)
		throws IOException
	{
		return getLineNumberReader(is, file, UTF8, useMultiThreadedReader);
	}

	/**
	 * Creates and returns a new {@link AnchorLineNumberReader} on the given
	 * stream, unzipping the file on the fly if the given <code>file</code>'s
	 * name ends with ".gz". The stream <code>is</code> is expected to have been created on the
	 * given <code>file</code>.<p>
	 *
	 * This method uses the character set <code>cs</code> to do byte
	 * to character conversion.
	 *
	 * @param useMultiThreadedReader
	 *            If true, return a {@link MultiThreadedLineNumberReader} that
	 *            reads lines from the backing file in a separate thread, which
	 *            is useful if getting lines from the backing file is compute
	 *            intensive, such as when reading from a compressed file and
	 *            decompressing on-the-fly. If false, a regular
	 *            {@link AnchorLineNumberReader} is returned. If using a
	 *            multi-threaded reader and you do not read to the end of file,
	 *            you need to explicitly
	 *            {@link MultiThreadedLineNumberReader#close() close} the
	 *            reader, or the background reading thread will remain blocked,
	 *            and your program will not exit.
	 * @throws IOException
	 *             If there is an error opening the reader on the given stream.
	 */
	public static AnchorLineNumberReader getLineNumberReader(
		InputStream is, File file, Charset cs, boolean useMultiThreadedReader) throws IOException
	{
		InputStream is2 = wrapDecompressor(is, file);
		Reader r = new InputStreamReader(is2, cs);
		return getLineNumberReader(r, useMultiThreadedReader);
	}


	private static AnchorLineNumberReader getLineNumberReader(Reader r, boolean useMultiThreadedReader) {
		return useMultiThreadedReader
			? new MultiThreadedLineNumberReader(r)
			: new AnchorLineNumberReader(r);
	}

	// ------------------------------------------------- writer creation

	/**
	 * Returns a BufferedWriter on the given <code>file</code> using
	 * {@link #UTF8} to do byte to character conversion.
	 *
	 * @throws IOException
	 *             If there is an error creating the BufferedWriter.
	 * @see #getBufferedWriter(File, boolean)
	 */
	public static BufferedWriter getBufferedWriter(File file) throws IOException {
		return getBufferedWriter(file, false);
	}

	/**
	 * Returns a BufferedWriter on a {@link GZIPOutputStream} on the given
	 * <code>file</code>, using {@link #UTF8} to do byte
	 * to character conversion.
	 *
	 * @throws IOException
	 *             If there is an error creating the BufferedWriter.
	 * @see #getBufferedWriter(File, boolean)
	 */
	public static BufferedWriter getBufferedGzipWriter(File file) throws IOException {
		return getBufferedWriter(file, true);
	}

	/**
	 * Returns a BufferedWriter on the given <code>file</code> using
	 * {@link #UTF8} to do character-to-byte conversion.
	 *
	 * @param gzip
	 *            If true, then the file will be compressed using a
	 *            {@link GZIPOutputStream}.
	 *
	 * @throws IOException
	 *             If there is an error opening the BufferedWriter on the given
	 *             file.
	 */
	public static BufferedWriter getBufferedWriter(File file, boolean gzip) throws IOException {
		OutputStream fos = new FileOutputStream(file);
		if (gzip) {
			fos = new GZIPOutputStream(fos);
		}
		return new BufferedWriter(new OutputStreamWriter(fos, UTF8));
	}

}
