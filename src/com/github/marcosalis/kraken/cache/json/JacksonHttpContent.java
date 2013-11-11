/*
 * Copyright 2013 Luluvise Ltd
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
package com.github.marcosalis.kraken.cache.json;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.concurrent.NotThreadSafe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.json.Json;
import com.google.common.base.Preconditions;

/**
 * Simple extension of {@link AbstractHttpContent} to be used with JSON content
 * to be mapped with the default Jackson parser.
 * 
 * @deprecated
 * @since 1.0
 * @author Marco Salis
 */
@Deprecated
@NotThreadSafe
public class JacksonHttpContent extends AbstractHttpContent {

	private static final ObjectMapper JSON_MAPPER = JacksonJsonManager.getObjectMapper();

	/** JSON key name/value data. */
	private final Object data;

	public JacksonHttpContent(Object data) {
		super(Json.MEDIA_TYPE);
		this.data = Preconditions.checkNotNull(data);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		JSON_MAPPER.writeValue(out, data);
	}

	/**
	 * Returns the data object associated with this HttpContent
	 */
	public Object getData() {
		return data;
	}

}