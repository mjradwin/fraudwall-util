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
package com.fraudwall.util.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fraudwall.util.Utilities;
import com.fraudwall.util.io.IOUtils;

/**
 * Facility for substituting parameter values for parameter names in (SQL) template
 * files located in a directory subtree. Template files have the extension
 * ".tmpl". Lines that start with the literal string "-- param" in the template
 * file are ignored. By convention, parameter names begin with a "$" character,
 * but this class does not require that.<p>
 *
 * @author ryan
 */
public class TemplateExpander {
	private static final String PARAM_PREFIX = "-- param";
	private static final String TEMPLATE_FILE_EXTENSION = ".tmpl";

	/**
	 * Global cache mapping root directories to template maps. This cache
	 * prevents the constructor from having to walk the same directory
	 * tree more than once.
	 */
	private static final Map<File, Map<String,File>> templatesCache =
		new HashMap<File, Map<String,File>>();

	/** Maps template file names to their respective locations in the file system. */
	private Map<String,File> templates;

	/**
	 * The root directory of the tree searched by the constructor for SQL template files.
	 * Defaults to "$ROOT/sql".
	 */
	private static File sqlTemplateRootDir = new File(IOUtils.getRootDir(), "sql");

	/**
	 * Sets the directory to be used by this class's constructor as the root of
	 * the subtree containing all SQL template files. Defaults to "$ROOT/sql",
	 * but may be set to a different value for testing purposes.
	 */
	public static void setSqlTemplateRootDir(File dir) {
		sqlTemplateRootDir = dir;
	}

	/**
	 * Creates a new template expander capable of expanding all ".tmpl" files
	 * located in the directory tree rooted at the {@link #setSqlTemplateRootDir(File)
	 * sqlTemplateRootDir}.
	 */
	public TemplateExpander() {
		synchronized (templatesCache) {
			templates = templatesCache.get(sqlTemplateRootDir);
			if (templates == null) {
				templates = new HashMap<String, File>();
				addTemplates(sqlTemplateRootDir);
				templatesCache.put(sqlTemplateRootDir, templates);
			}
		}
	}

	/**
	 * Finds all ".tmpl" files in the directory tree rooted at <code>directory</code>
	 * and makes them available for use with {@link #expandTemplate(File, Map)}.
	 */
	private void addTemplates(File directory) {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				addTemplates(file);
			} else if (file.getName().endsWith(TEMPLATE_FILE_EXTENSION)) {
				templates.put(file.getName(), file);
			}
		}
	}

	/**
	 * Returns a newly created file object with the contents of the specified template name
	 * and having all parameters in the file replaced by their specified values.
	 */
	public File expandTemplate(String templateFileName, Map<String, String> params) throws IOException {
		if (!templates.containsKey(templateFileName)) {
			throw new IllegalArgumentException("Unknown template " + templateFileName);
		}
		return expandTemplate(templates.get(templateFileName), params);
	}

	/**
	 * Returns a newly created file object with the contents of the specified template file
	 * and having all parameters in the file replaced by their specified values.
	 */
	public File expandTemplate(File templateFile, Map<String, String> params) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(templateFile));
		try {
			String templateName = templateFile.getName();
			String suffix = Utilities.lastComponent(templateName);
			File expandedFile = File.createTempFile(templateName, "." + suffix);
			BufferedWriter writer = new BufferedWriter(new FileWriter(expandedFile));
			try {
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						break;
					}
					writer.write(expandLine(line, params));
					writer.newLine();
				}
				return expandedFile;
			} finally {
				writer.close();
			}
		} finally {
			reader.close();
		}
	}

	/**
	 * Expands all the parameters in the specified line, unless the line
	 * begins with "-- param".
	 */
	/*test*/ String expandLine(String line, Map<String, String> params) {
		if (!line.startsWith(PARAM_PREFIX)) {
			List<Map.Entry<String, String>> paramList = new ArrayList<Map.Entry<String, String>>(params.entrySet());
			Collections.sort(paramList, new Comparator<Map.Entry<String,String>>() {
				public int compare(Entry<String, String> o1, Entry<String, String> o2) {
					return o2.getKey().compareTo(o1.getKey());
				}
			});
			for (Map.Entry<String,String> entry: paramList) {
				line = line.replace(entry.getKey(), entry.getValue());
			}
		}
		return line;
	}
}
