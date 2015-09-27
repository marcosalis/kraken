/*
 * Copyright 2013 Luluvise Ltd
 * Copyright 2013 Marco Salis - fast3r(at)gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.marcosalis.kraken.utils;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.google.common.annotations.Beta;

/**
 * Helper class that contains static methods to perform common complex
 * operations on the file system.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public class FileUtils {

	private FileUtils() {
		// hidden constructor, no instantiation needed
	}

	/**
	 * Delete a directory and its whole content (files, sub-directories).
	 * 
	 * @param directory
	 *            The directory to remove
	 * @return True if successful, false otherwise
	 */
	public static boolean deleteDirectoryTree(@Nonnull File directory) {

		// if the directory already doesn't exist, we're successful
		if (!directory.exists()) {
			return true;
		}

		// not a directory, can't continue
		if (!directory.isDirectory()) {
			return false;
		}

		boolean result = true;
		final Stack<File> deletionStack = new Stack<File>();
		deletionStack.push(directory);

		while (!deletionStack.isEmpty()) {
			File toDelete = deletionStack.peek();
			if (toDelete.isDirectory()) {
				File[] children = toDelete.listFiles();
				if (children != null && children.length > 0) {
					for (File child : children) {
						deletionStack.push(child);
					}
				} else {
					deletionStack.pop();
					result = result && toDelete.delete();
				}
			} else {
				deletionStack.pop();
				result = result && toDelete.delete();
			}
		}
		return result;
	}

	/**
	 * Create directory full path if it doesn't exist, deleting any existing
	 * file with the same name if present.
	 * 
	 * @param directory
	 *            The directory path to create
	 * @return true if the directory exists after this call, false otherwise
	 */
	public static boolean createDir(@Nonnull File directory) {
		if (!directory.exists() || directory.isFile()) {
			if (directory.isFile()) {
				if (!directory.delete()) {
					return false;
				}
			}
			return directory.mkdirs();
		}
		return true;
	}

	/**
	 * Create an empty file and its full path if needed.
	 * 
	 * @param file
	 * @return true if the file has been created, false if it already exists or
	 *         something went wrong
	 * @throws IllegalArgumentException
	 *             if the passed File is a directory
	 */
	public static boolean createNewFileAndPath(@Nonnull File file) {
		if (file.exists()) {
			return false;
		}

		String name = file.getName();
		if (name.equals("")) {
			throw new IllegalArgumentException(String.format("File name part missing in %s",
					file.toString()));
		}

		if (!createDir(new File(file.getParent()))) {
			// directory tree creation failed
			return false;
		}

		try {
			return file.createNewFile();
		} catch (IOException e) {
			return false;
		}
	}

}