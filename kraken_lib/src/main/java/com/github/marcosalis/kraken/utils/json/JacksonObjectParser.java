/*
 * Copyright 2013 Luluvise Ltd
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
package com.github.marcosalis.kraken.utils.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.ObjectParser;
import com.google.common.annotations.Beta;

/**
 * Parses JSON data into an data class of key/value pairs.
 * 
 * <p>
 * Implementation is thread-safe.
 * </p>
 * 
 * <p>
 * Sample usage:
 * </p>
 * 
 * <pre>
 * <code>
 *   static void setParser(HttpRequest request) {
 *     request.setParser(new JsonObjectParser(new JacksonFactory()));
 *   }
 * </code>
 * </pre>
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public class JacksonObjectParser implements ObjectParser {

	private final ObjectMapper mMapper;

	public JacksonObjectParser(ObjectMapper mapper) {
		mMapper = mapper;
	}

	public <T> T parseAndClose(InputStream in, Charset charset, Class<T> dataClass)
			throws IOException {
		// encoding is automatically detected by ObjectMapper
		return (T) mMapper.readValue(in, dataClass);
	}

	public Object parseAndClose(InputStream in, Charset charset, Type dataType) throws IOException {
		// encoding is automatically detected by ObjectMapper
		// TODO detect if passed class is consistent
		return mMapper.readValue(in, dataType.getClass());
	}

	public <T> T parseAndClose(Reader reader, Class<T> dataClass) throws IOException {
		return (T) mMapper.readValue(reader, dataClass);
	}

	public Object parseAndClose(Reader reader, Type dataType) throws IOException {
		// TODO detect if passed class is consistent
		return mMapper.readValue(reader, dataType.getClass());
	}
}
