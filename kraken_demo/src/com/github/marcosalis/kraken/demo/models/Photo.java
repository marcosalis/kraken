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

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.marcosalis.kraken.cache.json.JsonModel;

/**
 * POJO representation of a Facebook photo model.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class Photo extends JsonModel {

	private static final String PHOTO_ID = "photo_id";
	private static final String PHOTO_URL = "photo_url";

	private final String photo_id;
	private final String photo_url;

	@JsonCreator
	public Photo(@JsonProperty(PHOTO_ID) String photo_id, @JsonProperty(PHOTO_URL) String photo_url) {
		this.photo_id = photo_id;
		this.photo_url = photo_url;
	}

	@JsonProperty(PHOTO_ID)
	public String getPhotoId() {
		return photo_id;
	}

	@JsonProperty(PHOTO_URL)
	public String getPhotoUrl() {
		return photo_url;
	}

}