/*
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

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.common.hash.Hashing;

/**
 * Unit tests for the {@link HashUtils} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class HashUtilsTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetDefaultHash() {
		assertNotNull(HashUtils.getDefaultHash(""));
		assertNotNull(HashUtils.getDefaultHash("string"));
		assertNotNull(HashUtils.getDefaultHash("string1", "string2"));
	}

	public void testGetMD5Hash() {
		assertNotNull(HashUtils.getMD5Hash(""));
		assertNotNull(HashUtils.getMD5Hash("string"));
		assertNotNull(HashUtils.getMD5Hash("string1", "string2"));
	}

	public void testGetHash() {
		assertNotNull(HashUtils.getHash(Hashing.sha512(), ""));
		assertNotNull(HashUtils.getHash(Hashing.sha512(), "string"));
		assertNotNull(HashUtils.getHash(Hashing.sha512(), "string1", "string2"));
	}

}