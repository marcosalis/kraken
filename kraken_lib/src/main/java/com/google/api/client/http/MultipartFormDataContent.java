/*
 * Copyright 2013 Marco Salis
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.google.api.client.util.StreamingContent;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

/**
 * Serializes MIME "multipart/form-data" content as specified by <a
 * href="http://tools.ietf.org/html/rfc2388">RFC 2388: Returning Values from
 * Forms: multipart/form-data</a>
 * 
 * The implementation is a subclass of {@link MultipartContent} that sets the
 * media type to <code>"multipart/form-data"</code> and defaults to
 * {@link #DEFAULT_BOUNDARY} as a boundary string.
 * 
 * The generated content output differs from the superclass one to specifically
 * meet the <code>"multipart/form-data"</code> RFC specifications, for example
 * omitting redundant headers in the content parts (except for nested
 * "multipart" parts).
 * 
 * <p>
 * Shortcut method {@link #addPart(Part, String, String)} is provided in order
 * to easily set name and file name for the mandatory header
 * <code>"Content-Disposition"</code>. This header can be manually set in each
 * part's headers using the following format (but in this case no consistency
 * checks are made and the request will most likely fail):
 * </p>
 * 
 * <code>Content-Disposition: form-data; name="user"</code>
 * 
 * <p>
 * Specifications on the "content-disposition" header (RFC 2183):<br>
 * {@link http://tools.ietf.org/html/rfc2183}
 * </p>
 * 
 * For a reference on how to build a multipart/form-data request see:
 * <ul>
 * <li>{@link http://chxo.com/be2/20050724_93bf.html}</li>
 * <li>{@link http://www.faqs.org/rfcs/rfc1867.html}</li>
 * </ul>
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@NotThreadSafe
public class MultipartFormDataContent extends MultipartContent {

	protected static final String DEFAULT_BOUNDARY = "__0xKhTmLbOuNdArY__";

	private static final String TWO_DASHES = "--";
	// HTTP headers are case-insensitive
	private static final String CONTENT_DISPOSITION = "content-disposition";
	private static final String CONTENT_TRANSFER_ENCODING = "content-transfer-encoding";

	private static final String DISPOSITION_STRING = "form-data; name=\"%s\"";
	private static final String DISPOSITION_STRING_EXT = "form-data; name=\"%1$s\"; filename=\"%2$s\"";

	/**
	 * Factory method to create {@link HttpMediaType} with media type
	 * <code>"multipart/form-data"</code>
	 */
	protected static final HttpMediaType getMultipartFormDataMediaType() {
		return new HttpMediaType("multipart/form-data");
	}

	/**
	 * Creates a new empty {@link MultipartFormDataContent}.
	 */
	public MultipartFormDataContent() {
		final HttpMediaType mediaType = getMultipartFormDataMediaType();
		// we're making setMediaType() final, can be called from constructor
		setMediaType(mediaType.setParameter("boundary", DEFAULT_BOUNDARY));
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		final OutputStreamWriter writer = new OutputStreamWriter(out, getCharset());
		final String boundary = getBoundary();

		final Collection<Part> parts = getParts();
		// iterate over each http content part
		for (Part part : parts) {
			final HttpHeaders headers = new HttpHeaders().setAcceptEncoding(null);
			final HttpHeaders partHeaders = part.headers;
			final HttpContent content = part.content;
			final String contentType = content != null ? content.getType() : null;
			String contentDisposition = null;

			if (contentType != null && contentType.contains("multipart") && partHeaders != null) {
				// copy all headers if this is a nested multipart content
				headers.fromHttpHeaders(partHeaders);
			} else {
				// we don't need all headers, just null out the unneeded ones
				headers.setContentEncoding(null).setUserAgent(null).setContentType(null)
						.setContentLength(null).set(CONTENT_TRANSFER_ENCODING, null);
				// add mandatory "content-disposition" header
				if (partHeaders != null) {
					contentDisposition = partHeaders.getFirstHeaderStringValue(CONTENT_DISPOSITION);
				}
				headers.set(CONTENT_DISPOSITION, contentDisposition);
			}

			// analyze the content
			StreamingContent streamingContent = null;
			if (content != null) {
				headers.setContentType(contentType);
				headers.set(CONTENT_TRANSFER_ENCODING, "binary");
				final HttpEncoding encoding = part.encoding;
				if (encoding == null) {
					streamingContent = content;
				} else {
					streamingContent = new HttpEncodingStreamingContent(content, encoding);
				}
			}

			// add boundary string - DON'T put more than one CR_LF between lines
			writer.write(TWO_DASHES);
			writer.write(boundary);
			writer.write(NEWLINE);

			// write headers
			HttpHeaders.serializeHeadersForMultipartRequests(headers, null, null, writer);

			// write content if any
			if (streamingContent != null) {
				writer.write(NEWLINE);
				writer.flush();
				streamingContent.writeTo(out);
				writer.write(NEWLINE);
			}
		}

		// final string boundary
		writer.write(TWO_DASHES);
		writer.write(boundary);
		writer.write(TWO_DASHES);
		writer.flush(); // flush before returning
	}

	/**
	 * Adds an HTTP multipart part.
	 * 
	 * This is a shortcut method to allow adding the specified name and
	 * (optional) filename are added in the <code>"content-disposition"</code>
	 * headers for the content (as per RFC 2183 par. 2). If the header is
	 * already specified in the added part, its value is overridden.
	 * 
	 * @param part
	 *            The {@link Part} to add to the content
	 * @param dispositionName
	 *            The name of the part (usually the field name in a web form)
	 * @param dispositionFilename
	 *            The optional filename of the part (usually for adding a
	 *            {@link FileContent} part)
	 */
	public MultipartFormDataContent addPart(@Nonnull Part part, @Nonnull String dispositionName,
			@Nullable String dispositionFilename) {
		Preconditions.checkNotNull(dispositionName);
		String value = null;
		if (dispositionFilename == null) {
			value = String.format(Locale.US, DISPOSITION_STRING, dispositionName);
		} else { // append filename if not null
			value = String.format(Locale.US, DISPOSITION_STRING_EXT, dispositionName,
					dispositionFilename);
		}
		if (part.headers == null) {
			part.headers = new HttpHeaders().setAcceptEncoding(null);
		}
		part.headers.set(CONTENT_DISPOSITION, value);
		return (MultipartFormDataContent) super.addPart(part);
	}

	/* Overriding this superclass methods just to change the return type */

	@Override
	public MultipartFormDataContent addPart(@Nonnull Part part) {
		return (MultipartFormDataContent) super.addPart(part);
	}

	@Override
	public MultipartFormDataContent setParts(@Nonnull Collection<Part> parts) {
		return (MultipartFormDataContent) super.setParts(parts);
	}

	@Override
	public MultipartFormDataContent setContentParts(
			@Nonnull Collection<? extends HttpContent> contentParts) {
		return (MultipartFormDataContent) super.setContentParts(contentParts);
	}

	@Override
	public final MultipartFormDataContent setMediaType(@Nullable HttpMediaType mediaType) {
		super.setMediaType(mediaType);
		return this;
	}

	/**
	 * Sets the boundary string to use (must be not null)
	 * 
	 * If this is not called, the boundary defaults to {@link #DEFAULT_BOUNDARY}
	 * 
	 * @param boundary
	 *            The new boundary for the content
	 * @throws NullPointerException
	 *             if boundary is null
	 */
	@Override
	public MultipartFormDataContent setBoundary(@Nonnull String boundary) {
		return (MultipartFormDataContent) super.setBoundary(boundary);
	}

}