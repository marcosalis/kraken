/*
 * Copyright 2013 Marco Salis - fast3r@gmail.com
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
package com.google.api.client.http;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;
import android.util.Log;

import com.google.api.client.http.MultipartContent.Part;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.Json;
import com.google.api.client.json.jackson2.JacksonFactory;

/**
 * Tests {@link MultipartFormDataContent}.
 * 
 * @since 1.0
 * @author Marco Salis
 */
public class MultipartFormDataContentTest extends TestCase {

	private static final String CRLF = "\r\n";

	private Part bytesContent;
	private Part jsonContent;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		bytesContent = new Part(ByteArrayContent.fromString(Json.MEDIA_TYPE, "testcontent"));
		jsonContent = new Part(new JsonHttpContent(new JacksonFactory(), new String("{}")));
	}

	public void testContent_single() throws Exception {
		final MultipartFormDataContent content = new MultipartFormDataContent();
		content.addPart(jsonContent, "jsonField", null);

		final String disposition = "content-disposition: form-data; name=\"jsonField\"";
		final String boundary = content.getBoundary();

		final String expContent = "--" + boundary
				+ CRLF // start line
				+ "Content-Type: application/json; charset=UTF-8" + CRLF
				+ "content-transfer-encoding: binary" + CRLF //
				+ disposition + CRLF //
				+ CRLF //
				+ "\"{}\"" + CRLF //
				+ "--" + boundary + "--"; // end line

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		content.writeTo(out);
		final String string = out.toString();
		// TODO: remove logs
		Log.w("expected_single", expContent);
		Log.e("stream_single", string);

		assertEquals(expContent, string);
	}

	public void testContent_multiple() throws Exception {
		final MultipartFormDataContent content = new MultipartFormDataContent();
		jsonContent.headers = new HttpHeaders().setAcceptEncoding(null).set("content-disposition",
				"form-data; name=\"field1\"");
		content.addPart(jsonContent);
		content.addPart(bytesContent, "field2", null);

		final String boundary = content.getBoundary();

		final String expContent = "--" + boundary
				+ CRLF // start line - 1st part
				+ "Content-Type: application/json; charset=UTF-8" + CRLF
				+ "content-transfer-encoding: binary" + CRLF //
				+ "content-disposition: form-data; name=\"field1\"" + CRLF //
				+ CRLF //
				+ "\"{}\"" + CRLF // end first part
				+ "--" + boundary + CRLF // second part
				+ "Content-Type: application/json; charset=UTF-8" + CRLF //
				+ "content-transfer-encoding: binary" + CRLF //
				+ "content-disposition: form-data; name=\"field2\"" + CRLF //
				+ CRLF //
				+ "testcontent" + CRLF // end second part
				+ "--" + boundary + "--"; // end line

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		content.writeTo(out);
		final String contentString = out.toString();
		// TODO: remove logs
		Log.w("expected_multiple", expContent);
		Log.e("stream_multiple", contentString);

		assertEquals(expContent, contentString);
	}

	public void testContent_nestedMultipartBoundary() throws Exception {
		final MultipartFormDataContent content = new MultipartFormDataContent();
		content.addPart(jsonContent);

		final MultipartContent nested = new MultipartContent();
		nested.setBoundary("_nestedBoundary_");
		nested.addPart(bytesContent);
		content.addPart(new Part(nested), "nestedField", null);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		content.writeTo(out);
		final String contentString = out.toString();
		assertTrue(contentString.contains("_nestedBoundary_"));
		// TODO: remove logs
		Log.e("stream_nested", contentString);
	}

	public void testAddPart_withFilename() throws Exception {
		final MultipartFormDataContent content = new MultipartFormDataContent();

		assertNull(bytesContent.headers); // precondition
		content.addPart(bytesContent, "bytesField", "field.png");
		final String contentDisposition = bytesContent.headers
				.getFirstHeaderStringValue("content-disposition");
		assertEquals("form-data; name=\"bytesField\"; filename=\"field.png\"", contentDisposition);
	}

	public void testAddPart_nullHeaders() throws Exception {
		final MultipartFormDataContent content = new MultipartFormDataContent();

		assertNull(bytesContent.headers); // precondition
		content.addPart(bytesContent, "bytesField", null);
		final String contentDisposition = bytesContent.headers
				.getFirstHeaderStringValue("content-disposition");
		assertEquals("form-data; name=\"bytesField\"", contentDisposition);
	}

	public void testAddPart_existingHeaders() throws Exception {
		final MultipartFormDataContent content = new MultipartFormDataContent();

		bytesContent.headers = new HttpHeaders();
		content.addPart(bytesContent, "bytesField", null);
		final String contentDisposition = bytesContent.headers
				.getFirstHeaderStringValue("content-disposition");
		assertEquals("form-data; name=\"bytesField\"", contentDisposition);
	}

}