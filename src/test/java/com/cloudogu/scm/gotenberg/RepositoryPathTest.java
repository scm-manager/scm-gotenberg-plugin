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

import org.junit.jupiter.api.Test;
import sonia.scm.repository.NamespaceAndName;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryPathTest {

  private final RepositoryPath path = new RepositoryPath(
    "hitchhiker", "guide", "42", "a/b/c/h2g2.pdf"
  );

  @Test
  void shouldReturnNamespaceAndName() {
    NamespaceAndName namespaceAndName = path.getNamespaceAndName();
    assertThat(namespaceAndName.getNamespace()).isEqualTo(path.getNamespace());
    assertThat(namespaceAndName.getName()).isEqualTo(path.getName());
  }

  @Test
  void shouldReturnFilename() {
    assertThat(path.getFilename()).isEqualTo("h2g2.pdf");
  }

  @Test
  void shouldReturnExtension() {
    assertThat(path.getExtension()).contains("pdf");
  }

  @Test
  void shouldReturnCacheKey() {
    assertThat(path.getCacheKey()).isEqualTo("96e53063e4cd0cb27984756493989162c828865b7dfe1d3ab5782364dc993144");
  }

}
