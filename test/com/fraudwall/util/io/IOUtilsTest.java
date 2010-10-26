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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import com.fraudwall.util.AbstractPropsTest;
import com.fraudwall.util.AnchorFatalError;
import com.fraudwall.util.FWPropsTest;

public class IOUtilsTest extends AbstractPropsTest {

	/** {@link IOUtils#deleteOldFiles(File, long, String, boolean)} ---------------------------------------- */

	public void testDeleteOldFilesDeletesOldFiles() throws Exception {
		String[] files = new String[] { "a.csv", "b.csv", "c.csv", "a.txt", "b.txt", "c.txt" };
		File dir = getCreatedOutputDir();
		createFiles(dir, files);
		IOUtils.deleteOldFiles(dir, System.currentTimeMillis() + 1, null, false);
		checkFilesDoNotExist(dir, files);
	}

	public void testDeleteOldFilesSkipsNewFiles() throws Exception {
		String[] files = new String[] { "a.csv", "b.csv", "c.csv", "a.txt", "b.txt", "c.txt" };
		File dir = getCreatedOutputDir();
		createFiles(dir, files);
		IOUtils.deleteOldFiles(dir, 1, null, false);
		checkFilesExist(dir, files);
	}

	public void testDeleteOldFilesOnlyDeletesFilesMatchingPattern() throws Exception {
		String[] csvFiles = new String[] { "a.csv", "b.csv", "c.csv" };
		String[] txtFiles = new String[] { "a.txt", "b.txt", "c.txt" };
		File dir = getCreatedOutputDir();
		createFiles(dir, csvFiles);
		createFiles(dir, txtFiles);
		IOUtils.deleteOldFiles(dir, System.currentTimeMillis() + 1, ".*csv", false);
		checkFilesDoNotExist(dir, csvFiles);
		checkFilesExist(dir, txtFiles);
	}

	public void testDeleteOldFilesDoesNotRecurseWhenRecurseIsFalse() throws Exception {
		String[] files = new String[] { "a.csv", "b.csv", "c.csv", "a.txt", "b.txt", "c.txt" };
		File dir = getCreatedOutputDir();
		File tmp = new File(dir, "tmp");
		tmp.mkdir();
		assertTrue(tmp.exists());
		createFiles(tmp, files);
		IOUtils.deleteOldFiles(dir, System.currentTimeMillis() + 1, null, false);
		checkFilesExist(tmp, files);
	}

	public void testDeleteOldFilesDoesRecurseWhenRecurseIsTrue() throws Exception {
		String[] files = new String[] { "a.csv", "b.csv", "c.csv", "a.txt", "b.txt", "c.txt" };
		File dir = getCreatedOutputDir();
		File tmp = new File(dir, "tmp");
		tmp.mkdir();
		assertTrue(tmp.exists());
		createFiles(tmp, files);
		IOUtils.deleteOldFiles(dir, System.currentTimeMillis() + 1, null, true);
		checkFilesDoNotExist(tmp, files);
	}

	/** {@link IOUtils#truncateOldFiles(File, long, String, boolean)} ---------------------------------------- */

	public void testTruncateOldFilesTruncatesOldFiles() throws Exception {
		String[] files = new String[] { "a.csv", "b.csv", "c.csv", "a.txt", "b.txt", "c.txt" };
		File dir = getCreatedOutputDir();
		createFiles(dir, files);
		IOUtils.truncateOldFiles(dir, System.currentTimeMillis() + 1, null, false);
		checkFilesAreEmpty(dir, files);
	}

	public void testTruncateOldFilesSkipsNewFiles() throws Exception {
		String[] files = new String[] { "a.csv", "b.csv", "c.csv", "a.txt", "b.txt", "c.txt" };
		File dir = getCreatedOutputDir();
		createFiles(dir, files);
		IOUtils.truncateOldFiles(dir, 1, null, false);
		checkFilesAreNotEmpty(dir, files);
	}

	public void testTruncateOldFilesOnlyTruncatesFilesMatchingPattern() throws Exception {
		String[] csvFiles = new String[] { "a.csv", "b.csv", "c.csv" };
		String[] txtFiles = new String[] { "a.txt", "b.txt", "c.txt" };
		File dir = getCreatedOutputDir();
		createFiles(dir, csvFiles);
		createFiles(dir, txtFiles);
		IOUtils.truncateOldFiles(dir, System.currentTimeMillis() + 1, ".*csv", false);
		checkFilesAreEmpty(dir, csvFiles);
		checkFilesAreNotEmpty(dir, txtFiles);
	}

	public void testTruncateOldFilesDoesNotRecurseWhenRecurseIsFalse() throws Exception {
		String[] files = new String[] { "a.csv", "b.csv", "c.csv", "a.txt", "b.txt", "c.txt" };
		File dir = getCreatedOutputDir();
		File tmp = new File(dir, "tmp");
		tmp.mkdir();
		assertTrue(tmp.exists());
		createFiles(tmp, files);
		IOUtils.truncateOldFiles(dir, System.currentTimeMillis() + 1, null, false);
		checkFilesAreNotEmpty(tmp, files);
	}

	public void testTruncateOldFilesDoesRecurseWhenRecurseIsTrue() throws Exception {
		String[] files = new String[] { "a.csv", "b.csv", "c.csv", "a.txt", "b.txt", "c.txt" };
		File dir = getCreatedOutputDir();
		File tmp = new File(dir, "tmp");
		tmp.mkdir();
		assertTrue(tmp.exists());
		createFiles(tmp, files);
		IOUtils.truncateOldFiles(dir, System.currentTimeMillis() + 1, null, true);
		checkFilesAreEmpty(tmp, files);
	}

	/** {@link IOUtils#deleteDirectoryTree(File)} ------------------------------------------------- */

	public void testDeleteDirectoryTreeRemovesAllFilesAndDirectories() throws Exception {
		File rootDir = getCreatedOutputDir();
		File subDir = new File(rootDir, "dir");
		IOUtils.createDirectoryIfNeeded(subDir);
		new File(rootDir, "file1").createNewFile();
		new File(subDir, "file2").createNewFile();
		IOUtils.deleteDirectoryTree(rootDir);
		assertFalse(rootDir.exists());
	}

	// ---------------------------------------------- privateHelpers

	private void checkFilesDoNotExist(File directory, String... files) {
		for (String fileName : files) {
			File file = new File(directory, fileName);
			assertFalse(file.getName() + " exists", file.exists());
		}
	}

	private void checkFilesExist(File directory, String[] files) {
		for (String fileName : files) {
			File file = new File(directory, fileName);
			assertTrue(file.getName() + " does not exist", file.exists());
		}
	}

	private void checkFilesAreNotEmpty(File directory, String[] files) {
		for (String fileName : files) {
			File file = new File(directory, fileName);
			assertNotEquals(0, file.length(), file.getName() + " is empty");
		}
	}

	private void checkFilesAreEmpty(File directory, String[] files) {
		for (String fileName : files) {
			File file = new File(directory, fileName);
			assertEquals(file.getName() + " is not empty", 0, file.length());
		}
	}

	private static void createFiles(File directory, String[] fileNames) throws Exception {
		for (String fileName : fileNames) {
			FileWriter writer = new FileWriter(new File(directory, fileName));
			writer.write("not empty");
			writer.close();
			assertTrue(new File(directory, fileName).exists());
		}
	}

	/** {@link IOUtils#listFiles(File, boolean, String...)} ------------------------------------------------- */

	public void testListFilesFindsAllMatchingFilesInDirectory() throws IOException {
		File[] files = createFilesInDir(getCreatedOutputDir(), "a.csv", "b.csv");
		assertListEquals(files, IOUtils.listFiles(getCreatedOutputDir(), false, "csv"));
	}

	public void testListFilesFindsFilesWithAnyExtensionInDirectory() throws IOException {
		File[] files = createFilesInDir(getCreatedOutputDir(), "a.csv", "b.csv", "c.gz", "d.gz");
		createFilesInDir(getCreatedOutputDir(), "a.txt", "b.sh");
		assertListEquals(files, IOUtils.listFiles(getCreatedOutputDir(), false, "csv", "gz"));
	}

	public void testListFilesSortsOutput() throws IOException {
		File[] files = createFilesInDir(getCreatedOutputDir(), "a.csv", "d.csv", "b.csv", "c.csv");
		Arrays.sort(files);
		assertListEquals(files, IOUtils.listFiles(getCreatedOutputDir(), false, "csv"));
	}

	public void testListFilesRecursesWhenRecurseIsTrue() throws IOException {
		File[] files = createFilesInDir(getCreatedOutputDir(), "a.csv", "b/a.csv", "b/c.csv", "c/c.csv", "d.csv");
		assertListEquals(files, IOUtils.listFiles(getCreatedOutputDir(), true, "csv"));
	}

	public void testListFilesDoesNotRecursesWhenRecurseIsFalse() throws IOException {
		File[] files = createFilesInDir(getCreatedOutputDir(), "a.csv", "d.csv");
		createFilesInDir(getCreatedOutputDir(), "b/a.csv", "b/c.csv", "c/c.csv");
		assertListEquals(files, IOUtils.listFiles(getCreatedOutputDir(), false, "csv"));
	}

	private File[] createFilesInDir(File dir, String... names)  throws IOException {
		File[] files = new File[names.length];
		for (int i=0; i < names.length; i++) {
			File file = new File(dir, names[i]);
			IOUtils.createDirectoryIfNeeded(file.getParentFile());
			file.createNewFile();
			files[i] = file;
		}
		return files;
	}

	/** {@link IOUtils#listFiles(File, IOFileFilter)} --------------------------------------------------- */

	public void testListFilesWithFilterFindsAllMatchingFilesInDirectory() throws IOException {
		File[] files = createFilesInDir(getCreatedOutputDir(), "a.csv", "b.csv");
		assertListEquals(files, IOUtils.listFiles(getCreatedOutputDir(), makeCsvFilter()));
	}

	public void testListFilesWithFilterFindsDoesNotFindFilesThatDoNotMatch() throws IOException {
		File[] files = createFilesInDir(getCreatedOutputDir(), "a.csv", "b.csv");
		createFilesInDir(getCreatedOutputDir(), "foo.txt");
		assertListEquals(files, IOUtils.listFiles(getCreatedOutputDir(), makeCsvFilter()));
	}

	private IOFileFilter makeCsvFilter() {
		return new SuffixFileFilter("csv");
	}

	/** {@link IOUtils#createAtomicRenameFileFor(File)} --------------------------------------------------- */

	public void testCreateAtomicRenameFileForCreatesFileInSameDirectoryAsFile() throws Exception {
		File file = new File(getCreatedOutputDir(), "foo.csv");
		assertEquals(file.getParent(), IOUtils.createAtomicRenameFileFor(file).getParent());
	}

	public void testCreateAtomicRenameFileForCreatesFileWithSimilarNameAsFile() throws Exception {
		File file = new File(getCreatedOutputDir(), "foo.csv");
		File tmpFile = IOUtils.createAtomicRenameFileFor(file);
		assertTrue(tmpFile.getName().startsWith("." + file.getName()));
		assertTrue(tmpFile.getName().endsWith(".tmp"));
	}

	public void testCreateAtomicRenameFileForCreatesNewFileEachTime() throws Exception {
		File file = new File(getCreatedOutputDir(), "foo.csv");
		File tmpFile1 = IOUtils.createAtomicRenameFileFor(file);
		File tmpFile2 = IOUtils.createAtomicRenameFileFor(file);
		assertNotEquals(tmpFile1, tmpFile2);
	}

	/** {@link IOUtils#renameFile(File, File)} ----------------------------------------------------------- */

	public void testRenameFileCreatesFileWithNewName() throws Exception {
		File source = new File(getCreatedOutputDir(), "foo.csv");
		source.createNewFile();
		File target = new File(getCreatedOutputDir(), "foo.txt");
		IOUtils.renameFile(source, target);
		assertTrue(target.exists());
		assertFalse(source.exists());
	}

	public void testRenameFileThrowsIfSourceDoesNotExist() throws Exception {
		File source = new File(getCreatedOutputDir(), "foo.csv");
		File target = new File(getCreatedOutputDir(), "foo.txt");
		try {
			IOUtils.renameFile(source, target);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), source + " does not exist");
		}
	}

	public void testRenameFileReplacesTargetIfTargetExists() throws Exception {
		File source = new File(getCreatedOutputDir(), "foo.csv");
		source.createNewFile();
		File target = new File(getCreatedOutputDir(), "foo.txt");
		target.createNewFile();
		IOUtils.renameFile(source, target);
		assertTrue(target.exists());
		assertFalse(source.exists());
	}

	public void testRenameFileThrowsIfTargetDirectoryDoesNotExist() throws Exception {
		File source = new File(getCreatedOutputDir(), "foo.csv");
		source.createNewFile();
		File target = new File(getCreatedOutputDir(), "parent/foo.txt");
		try {
			IOUtils.renameFile(source, target);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), target.getParentFile() + " does not exist");
		}
	}

	public void testRenameFileThrowsIfRenameFails() throws Exception {
		File source = new File(getCreatedOutputDir(), "foo.csv");
		source.createNewFile();
		StringBuilder longFileName = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			longFileName.append("1234567890123456789012345678901234567890123456789012345678901234567890");
		}
		File target = new File(getCreatedOutputDir(), longFileName.toString());
		try {
			IOUtils.renameFile(source, target);
			fail();
		} catch (AnchorFatalError e) {
			// expected case
		}
	}

	/** {@link IOUtils#deleteAllFilesInDirectory(File)} --------------------------------------------------- */

	public void testDeleteAllFilesInDirectoryThrowsIfDirDoesNotExist() throws IOException {
		File dir = new File(getCreatedOutputDir(), "foo");
		try {
			IOUtils.deleteAllFilesInDirectory(dir);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(dir + " does not exist", e.getMessage());
		}
	}

	public void testDeleteAllFilesInDirectoryThrowsIfDirIsNotDirectory() throws IOException {
		File dir = new File(getCreatedOutputDir(), "foo");
		dir.createNewFile();
		try {
			IOUtils.deleteAllFilesInDirectory(dir);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(dir + " is not a directory", e.getMessage());
		}
	}

	public void testDeleteAllFilesInDirectoryDoesNotRemoveDirectory() throws IOException {
		IOUtils.deleteAllFilesInDirectory(getCreatedOutputDir());
		assertTrue(getCreatedOutputDir().exists());
	}

	public void testDeleteAllFilesInDirectoryRemovesAllFiles() throws IOException {
		new File(getCreatedOutputDir(), "a.txt").createNewFile();
		new File(getCreatedOutputDir(), "b.txt").createNewFile();
		IOUtils.createDirectoryIfNeeded(new File(getCreatedOutputDir(), "1"));
		new File(getCreatedOutputDir(), "1/c.txt").createNewFile();
		new File(getCreatedOutputDir(), "1/d.txt").createNewFile();
		assertEquals(4, IOUtils.listFiles(getCreatedOutputDir(), /*recurse=*/ true, "txt").size());
		IOUtils.deleteAllFilesInDirectory(getCreatedOutputDir());
		assertEquals(0, getCreatedOutputDir().listFiles().length);
	}

	/** {@link IOUtils#moveFilesToDirectory(File, File...)} ----------------------------------------------- */

	public void testMoveFilesToDirectoryThrowsIfDirDoesNotExist() throws IOException {
		File dir = new File(getCreatedOutputDir(), "foo");
		try {
			IOUtils.moveFilesToDirectory(dir);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Argument 'directory' does not exist " + dir, e.getMessage());
		}
	}

	public void testMoveFilesToDirectoryThrowsIfDirIsNotDirectory() throws IOException {
		File dir = new File(getCreatedOutputDir(), "foo");
		dir.createNewFile();
		try {
			IOUtils.moveFilesToDirectory(dir);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Argument 'directory' is not a directory " + dir, e.getMessage());
		}
	}

	public void testMoveFilesToDirectoryMovesAllFiles() throws IOException {
		File dstDir = new File(getCreatedOutputDir(), "dst");
		IOUtils.createDirectoryIfNeeded(dstDir);
		File[] files = createFilesInDir(new File(getCreatedOutputDir(), "src"), "a.csv", "b.csv");
		IOUtils.moveFilesToDirectory(dstDir, files);
		for (File file : files) {
			assertFalse("File was not removed " + file, file.exists());
			assertTrue("File was not created " + new File(dstDir, file.getName()), new File(dstDir, file.getName()).exists());
		}
	}

	/** {@link IOUtils#atomicMoveFileToDirectory(File, File)} ----------------------------------------------- */

	public void testAtomicMoveFileToDirectoryMovesFile() throws IOException {
		File dstDir = new File(getCreatedOutputDir(), "dst");
		IOUtils.createDirectoryIfNeeded(dstDir);
		File[] files = createFilesInDir(new File(getCreatedOutputDir(), "src"), "a.csv", "b.csv");
		for (File file : files) {
			IOUtils.atomicMoveFileToDirectory(file, dstDir);
			assertFalse("File was not removed " + file, file.exists());
			assertTrue("File was not created " + new File(dstDir, file.getName()), new File(dstDir, file.getName()).exists());
		}
	}

	/** {@link IOUtils#getClickLogCharacterEncoding()} ------------------------------------------------------ */

	public void testGetClickLogCharacterEncodingReturnsCorrectEncoding() {
		checkGetClickLogCharacterEncodingReturnsCorrectEncoding("UTF-8");
		checkGetClickLogCharacterEncodingReturnsCorrectEncoding("ISO-8859-1");
	}

	private void checkGetClickLogCharacterEncodingReturnsCorrectEncoding(String encoding) {
		IOUtils.CLICK_LOG_CHARACTER_ENCODING = null;
		FWPropsTest.setProperty("Customer.clickLogCharacterEncoding", encoding);
		assertEquals(Charset.forName(encoding), IOUtils.getClickLogCharacterEncoding());
	}
}
