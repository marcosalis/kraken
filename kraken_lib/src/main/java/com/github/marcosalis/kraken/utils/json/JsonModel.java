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
package com.github.marcosalis.kraken.utils.json;

import android.support.annotation.Nullable;
import android.widget.Filter;
import android.widget.Filterable;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.Immutable;

/**
 * General abstract class for a POJO (<i>Plain Old Java Object</i>) or DTO (<i>Data Transfer
 * Object</i>) model.
 *
 * Model concrete implementations must provide:
 *
 * <ul> <li>Meta-annotations for a Jackson {@link ObjectMapper} to match the object RESTful JSON
 * representations</li> <li>Getters for all their visible relevant fields</li> <li>Immutable or
 * anyway thread-safe model instances</li> </ul>
 *
 * {@link JsonModel} uses a global, static private {@link ObjectMapper} to perform
 * serialization/deserialization operations on its instances.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
@Immutable
@SuppressWarnings("deprecation")
public abstract class JsonModel {

    /**
     * Empty list global instance that can be used as a safer replacement of null when a
     * deserialized list is null in a model. Using a raw type is acceptable because the list itself
     * will never contain an object.
     */
    @SuppressWarnings("rawtypes")
    protected static final ImmutableList EMPTY_LIST = ImmutableList.of();

    /**
     * Empty map global instance that can be used as a safer replacement of null when a deserialized
     * list is null in a model. Using a raw type is acceptable because the map itself will never
     * contain an object.
     */
    @SuppressWarnings("rawtypes")
    protected static final ImmutableMap EMPTY_MAP = ImmutableMap.of();

    private static final ObjectMapper JSON_MAPPER = JacksonJsonManager.getObjectMapper();

    /**
     * Interface to implement in {@link Filter} objects that filter on model objects.
     *
     * By implementing it for a specific model you can provide custom filtering and use the method
     * {@link #filterModel(CharSequence, JsonModel)} for a {@link Filterable}
     */
    public static interface JsonModelFilter<M> {
        public abstract boolean filterModel(CharSequence constraint, M model);
    }

    /**
     * <p> <b>Note:</b> This method is not overridden anymore. The default {@link Object#toString()}
     * implementation is returned. Use {@link #toJsonString()} instead. </p>
     *
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Returns a Json representation of this instance.<br> See {@link ObjectMapper#writeValueAsString(Object)}
     * for details.<br>
     *
     * Note that this implementation performs an actual serialization of a model object, avoid using
     * this method inside production code for logging purposes only.
     */
    @Nullable
    public String toJsonString() {
        return JacksonJsonManager.toJsonString(this);
    }

    /**
     * Creates a {@link JsonModel} instance by parsing its JSON string representation with the
     * default {@link ObjectMapper}.
     *
     * @param profileJson The model object string representation (can be null)
     * @param type        The type of the object to parse (use TypeClass[].class for an array of
     *                    models)
     * @return The instance of the given type or null if the string could not be parsed
     */
    @Nullable
    public static <E> E parseFromString(@Nullable String modelJson, Class<E> type) {
        E model = null;
        if (modelJson != null) {
            try {
                model = parseFromStringOrThrow(modelJson, type);
            } catch (IOException e) {
                // we ignore this
            }
        }
        return model;
    }

    /**
     * Creates a {@link JsonModel} instance by parsing its JSON string representation with the
     * default {@link ObjectMapper}, or throw an exception if not successful.
     *
     * @param profileJson The model object string representation (can be null)
     * @param type        The type of the object to parse (use TypeClass[].class for an array of
     *                    models)
     * @return The instance of the given type or null if the string could not be parsed
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Nullable
    public static <E> E parseFromStringOrThrow(@Nullable String modelJson, Class<E> type)
            throws JsonParseException, JsonMappingException, IOException {
        E model = null;
        if (modelJson != null) {
            model = JSON_MAPPER.readValue(modelJson, type);
        }
        return model;
    }

    /**
     * Creates a {@link List} of {@link JsonModel} instances by parsing its JSON string
     * representation with the default {@link ObjectMapper}, ignoring all exceptions.
     *
     * TODO: unit tests
     *
     * @param profileJson The model object string representation (can be null)
     * @param type        The type of the object to parse
     * @return The list of models or null if the string could not be parsed
     */
    @Nullable
    public static <E extends JsonModel> List<E> parseListFromString(@Nullable String modelJson,
                                                                    Class<E> type) {
        List<E> list = null;
        if (modelJson != null) {
            try {
                list = parseListFromStringOrThrow(modelJson, type);
            } catch (IOException e) {
                // we ignore this
            }
        }
        return list;
    }

    /**
     * Creates a {@link List} of {@link JsonModel} instances by parsing its JSON string
     * representation with the default {@link ObjectMapper}, or throw an exception if not
     * successful.
     *
     * TODO: unit tests
     *
     * @param profileJson The model object string representation (can be null)
     * @param type        The type of the object to parse
     * @return The list of models or null if the string could not be parsed
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Nullable
    public static <E extends JsonModel> List<E> parseListFromStringOrThrow(
            @Nullable String modelJson, Class<E> type) throws JsonParseException,
            JsonMappingException, IOException {
        ArrayList<E> list = null;
        if (modelJson != null) {
            list = JSON_MAPPER.readValue(modelJson, new TypeReference<List<E>>() {
            });
        }
        return list;
    }

}