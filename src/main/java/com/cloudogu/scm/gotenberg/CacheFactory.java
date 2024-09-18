/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.gotenberg;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.store.StoreException;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class CacheFactory {

  private static final Logger LOG = LoggerFactory.getLogger(CacheFactory.class);

  private static final String STORE = "gotenberg";

  private final BlobStoreFactory blobStoreFactory;
  private final Map<String, Cache> caches = new ConcurrentHashMap<>();
  private final int cacheSize;

  @Inject
  public CacheFactory(BlobStoreFactory blobStoreFactory) {
    this(blobStoreFactory, 20);
  }

  @VisibleForTesting
  CacheFactory(BlobStoreFactory blobStoreFactory, int cacheSize) {
    this.blobStoreFactory = blobStoreFactory;
    this.cacheSize = cacheSize;
  }

  public Cache get(Repository repository) {
    return caches.computeIfAbsent(
      repository.getId(),
      id -> new Cache(blobStoreFactory.withName(STORE).forRepository(repository).build())
    );
  }

  public class Cache {

    private final BlobStore blobStore;
    private final Map<String, Instant> lru = new HashMap<>();

    private Cache(BlobStore blobStore) {
      this.blobStore = blobStore;
      blobStore.getAll().forEach(blob -> lru.put(blob.getId(), Instant.now()));
      checkCacheSizeLimit();
    }

    public synchronized Optional<InputStream> get(RepositoryPath repositoryPath) {
      String cacheKey = repositoryPath.getCacheKey();
      Optional<Blob> optional = blobStore.getOptional(cacheKey);
      if (optional.isPresent()) {
        lru.put(cacheKey, Instant.now());
      }
      return optional.map(this::getInputStream);
    }

    private InputStream getInputStream(Blob blob) {
      try {
        return blob.getInputStream();
      } catch (IOException e) {
        throw new StoreException("failed to open input stream", e);
      }
    }

    public synchronized void set(RepositoryPath repositoryPath, InputStream content) throws IOException {
      String cacheKey = repositoryPath.getCacheKey();
      Blob blob = blobStore.create(cacheKey);
      try (OutputStream output = blob.getOutputStream()) {
        ByteStreams.copy(content, output);
        blob.commit();
      }
      lru.put(cacheKey, Instant.now());
      checkCacheSizeLimit();
    }

    private void checkCacheSizeLimit() {
      while (lru.size() > 0 && lru.size() > cacheSize) {
        LOG.debug("size limit of {} reached by {}, remove eldest entry", cacheSize, lru.size());
        removeEldest();
      }
    }

    private void removeEldest() {
      Map.Entry<String, Instant> eldest = null;
      for (Map.Entry<String, Instant> e : lru.entrySet()) {
        if (eldest == null || e.getValue().isBefore(eldest.getValue())) {
          eldest = e;
        }
      }

      if (eldest == null) {
        throw new IllegalStateException("remove eldest should not be called if the map is empty");
      }

      String key = eldest.getKey();
      LOG.debug("remove eldest entry from blob store: {}", key);
      blobStore.remove(key);
      lru.remove(key);
    }
  }
}
