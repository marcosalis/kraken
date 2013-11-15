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
package com.github.marcosalis.kraken.demo.utils;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public abstract class ListToArraySerializer<M> extends JsonSerializer<List<M>> {
	@Override
	public void serialize(List<M> value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		if (value != null) {
			jgen.writeStartArray();
			for (M model : value) {
				jgen.writeObject(model);
			}
			jgen.writeEndArray();

		} else { // write empty array
			jgen.writeStartArray();
			jgen.writeEndArray();
		}
	}

}