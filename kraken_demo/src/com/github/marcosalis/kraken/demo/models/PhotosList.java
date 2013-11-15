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
package com.github.marcosalis.kraken.demo.models;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.marcosalis.kraken.cache.json.JsonModel;
import com.github.marcosalis.kraken.demo.models.PhotosList.PhotoArrayToListDeserializer;
import com.github.marcosalis.kraken.demo.models.PhotosList.PhotoListToArraySerializer;
import com.github.marcosalis.kraken.demo.utils.ArrayToListDeserializer;
import com.github.marcosalis.kraken.demo.utils.ListToArraySerializer;
import com.google.common.collect.ImmutableList;

/**
 * JSON data model for a list of photos.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Immutable
@JsonSerialize(using = PhotoListToArraySerializer.class)
@JsonDeserialize(using = PhotoArrayToListDeserializer.class)
public class PhotosList extends JsonModel {

	private static final String DATA = "data";

	private final ImmutableList<Photo> photos;

	@JsonCreator
	@SuppressWarnings("unchecked")
	public PhotosList(@JsonProperty(DATA) List<Photo> photos) {
		this.photos = photos != null ? ImmutableList.copyOf(photos) : EMPTY_LIST;
	}

	@Nonnull
	@JsonProperty(DATA)
	public ImmutableList<Photo> getPhotos() {
		return photos;
	}

	public static class PhotoListToArraySerializer extends ListToArraySerializer<Photo> {
	}

	public static class PhotoArrayToListDeserializer extends ArrayToListDeserializer<Photo> {
		@Override
		protected TypeReference<List<Photo>> getTypeReference() {
			return new TypeReference<List<Photo>>() {
			};
		}
	}

}