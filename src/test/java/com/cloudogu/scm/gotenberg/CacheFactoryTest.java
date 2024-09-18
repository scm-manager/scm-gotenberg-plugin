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

import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryBlobStoreFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class CacheFactoryTest {

  @Test
  void shouldCache() throws IOException {
    Repository repository = RepositoryTestData.createHeartOfGold();

    CacheFactory factory = new CacheFactory(new InMemoryBlobStoreFactory());

    CacheFactory.Cache cache = factory.get(repository);

    RepositoryPath path = path(repository, "a.txt");
    cache.set(path, stream("Hello"));

    assertThat(cache.get(path)).hasValueSatisfying(stream -> hasContent(stream, "Hello"));
  }

  @Test
  void shouldRemoveEldest() throws IOException {
    Repository repository = RepositoryTestData.createHeartOfGold();
    CacheFactory factory = new CacheFactory(new InMemoryBlobStoreFactory(), 2);
    CacheFactory.Cache cache = factory.get(repository);

    cache.set(path(repository, "a.txt"), stream("Hello from a"));
    cache.set(path(repository, "b.txt"), stream("Hello from b"));
    cache.set(path(repository, "c.txt"), stream("Hello from c"));

    assertThat(cache.get(path(repository, "a.txt"))).isEmpty();
    assertThat(cache.get(path(repository, "b.txt")))
      .hasValueSatisfying(stream -> hasContent(stream, "Hello from b"));
    assertThat(cache.get(path(repository, "c.txt")))
      .hasValueSatisfying(stream -> hasContent(stream, "Hello from c"));
  }

  private void hasContent(InputStream stream, String expected) {
    try {
      byte[] bytes = ByteStreams.toByteArray(stream);
      assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo(expected);
    } catch (IOException e) {
      throw new IllegalStateException("failed to copy streams");
    }
  }

  private InputStream stream(String content) {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }

  private RepositoryPath path(Repository repository, String path) {
    return new RepositoryPath(repository.getNamespace(), repository.getName(), "42", path);
  }

}
