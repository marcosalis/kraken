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
import com.github.marcosalis.kraken.utils.json.JsonModel;
import com.google.common.collect.ImmutableList;

/**
 * JSON data model for a list of photos.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Immutable
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

}