/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
