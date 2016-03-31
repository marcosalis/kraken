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
package com.github.marcosalis.kraken.cache.internal;

import android.content.Context;
import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.SecondLevelCache.ClearMode;
import com.github.marcosalis.kraken.cache.internal.loaders.ContentLoader;
import com.github.marcosalis.kraken.cache.internal.loaders.ContentLoader.ContentUpdateCallback;
import com.github.marcosalis.kraken.cache.internal.loaders.ModelDiskContentLoaderFactory;
import com.github.marcosalis.kraken.cache.requests.CacheableRequest;
import com.github.marcosalis.kraken.utils.annotations.NotForUIThread;
import com.github.marcosalis.kraken.utils.json.JsonModel;
import com.google.common.annotations.Beta;

import java.io.File;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Generic, abstract extension of an {@link AbstractDiskModelContentProxy} that handles the
 * retrieval of {@link JsonModel}s that can be grouped in a list.
 *
 * This is useful to handle the case where a content can be provided as a single JSON model object
 * or as a list of models with the same representation, which is grouped in another {@link
 * JsonModel}. When this happens, we usually want to automatically update the "single model" cache
 * when we get a fresh list of models and invalidate the list of models when we get a fresh single
 * model.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
@ThreadSafe
public abstract class AbstractDiskModelListContentProxy<MODEL extends JsonModel, LIST extends JsonModel>
        extends AbstractDiskModelContentProxy<MODEL> implements ContentUpdateCallback<LIST> {

    private final ListContentProxy mListContentProxy;

    public AbstractDiskModelListContentProxy(@NonNull Context context,
                                             @NonNull ObjectMapper mapper, @NonNull Class<MODEL> modelClass, int modelsInCache,
                                             @NonNull Class<LIST> modelListClass, int listsInCache, @NonNull String diskFolder,
                                             final long expiration,
                                             @NonNull ModelDiskContentLoaderFactory<CacheableRequest<MODEL>, MODEL> loaderFactory,
                                             @NonNull ModelDiskContentLoaderFactory<CacheableRequest<LIST>, LIST> listLoaderFactory) {
        super(context, mapper, modelClass, modelsInCache, diskFolder, expiration, loaderFactory);
        final String subFolder = diskFolder + File.separator + "list";
        mListContentProxy = new ListContentProxy(context, mapper, modelListClass, listsInCache,
                subFolder, expiration, listLoaderFactory);
    }

    public AbstractDiskModelListContentProxy(@NonNull Context context,
                                             @NonNull ObjectMapper mapper, @NonNull Class<MODEL> modelClass, int modelsInCache,
                                             @NonNull Class<LIST> modelListClass, int listsInCache, @NonNull String diskFolder,
                                             final long expiration) {
        super(context, mapper, modelClass, modelsInCache, diskFolder, expiration);
        final String subFolder = diskFolder + File.separator + "list";
        mListContentProxy = new ListContentProxy(context, mapper, modelListClass, listsInCache,
                subFolder, expiration, null);
    }

    /**
     * Retrieves a model list from the content proxy. See {@link ContentLoader}.
     *
     * @param policy  The {@link AccessPolicy} to use
     * @param request The {@link CacheableRequest} for the list
     * @return The model list, or null of unsuccessful
     * @throws Exception
     */
    @NotForUIThread
    public final LIST getModelList(AccessPolicy policy, CacheableRequest<LIST> request)
            throws Exception {
        return mListContentProxy.getModel(policy, request, this);
    }

    @Override
    public abstract void onContentUpdated(LIST newContent);

    /**
     * See {@link AbstractDiskModelContentProxy#putModel(com.luluvise.android.api.model.JsonModel)}
     *
     * <b>Warning:</b> With the invalidate flag set to on, this call will invalidate all the model
     * list caches, in order not to retrieve outdated content from future cache queries.
     *
     * @param model The model to put into the cache
     */
    @NotForUIThread
    public final void putModel(final MODEL model, boolean invalidate) {
        super.putModel(model);
        // clear list caches to avoid stale data if needed
        if (invalidate) {
            clearListCache();
        }
    }

    /**
     * Uses {@link #putModel(JsonModel, boolean)} with invalidate flag set by default to true.
     */
    @Override
    @NotForUIThread
    public final void putModel(final MODEL model) {
        putModel(model, true);
    }

    /**
     * Forces a model list object to be put into the cache.<br> This must be ONLY used for injection
     * testing purposes.
     *
     * @param list The model list to put into the cache
     */
    public final void putModelList(String key, final LIST list) {
        if (list == null) {
            return; // fail-safe attitude
        }
        // update model into caches
        mListContentProxy.putModel(key, list);
        // TODO: call onContentUpdated?
    }

    /**
     * Clears the model lists memory and disk caches.
     *
     * It's safe to execute this method from the UI thread.
     */
    public void clearListCache() {
        mListContentProxy.clearCache();
    }

    @Override
    public void clearMemoryCache() {
        super.clearMemoryCache();
        mListContentProxy.clearMemoryCache();
    }

    @Override
    public void scheduleClearDiskCache() {
        super.scheduleClearDiskCache();
        mListContentProxy.scheduleClearDiskCache();
    }

    @Override
    @NotForUIThread
    public void clearDiskCache(ClearMode mode) {
        super.clearDiskCache(mode);
        mListContentProxy.clearDiskCache(mode);
    }

    /**
     * {@link AbstractDiskModelContentProxy} extension for a list model.
     */
    private class ListContentProxy extends AbstractDiskModelContentProxy<LIST> {

        public ListContentProxy(Context context, ObjectMapper mapper, Class<LIST> modelClass,
                                int modelsInCache, String diskFolder, long expiration,
                                ModelDiskContentLoaderFactory<CacheableRequest<LIST>, LIST> loaderFactory) {
            super(context, mapper, modelClass, modelsInCache, diskFolder, expiration, loaderFactory);
        }
    }

}