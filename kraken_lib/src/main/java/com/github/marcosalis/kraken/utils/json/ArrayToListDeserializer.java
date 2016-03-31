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
package com.github.marcosalis.kraken.utils.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.marcosalis.kraken.utils.android.LogUtils;
import com.google.common.annotations.Beta;

import java.io.IOException;
import java.util.List;

/**
 * Abstract generic {@link JsonDeserializer} that deserializes a JSON array to a list.
 *
 * @param <M> The model object type
 * @author Marco Salis
 * @since 1.0
 */
@Beta
public abstract class ArrayToListDeserializer<M> extends JsonDeserializer<List<M>> {
    @Override
    public List<M> deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext)
            throws IOException, JsonProcessingException {
        try {
            List<M> list = jsonparser.readValueAs(getTypeReference());
            return list;
        } catch (JsonProcessingException e) {
            LogUtils.logException(e);
            return null; // something wrong happened
        }
    }

    /**
     * Returns the parsed object type. A single, static instance is more efficient. Create it with:
     *
     * <code><pre>
     * new TypeReference<List<TYPE>>() {};
     * </pre></code>
     */
    protected abstract TypeReference<List<M>> getTypeReference();

}