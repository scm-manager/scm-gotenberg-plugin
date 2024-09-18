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
