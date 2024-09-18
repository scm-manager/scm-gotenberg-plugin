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

import com.google.common.hash.Hashing;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sonia.scm.repository.NamespaceAndName;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class RepositoryPath {

  private final String namespace;
  private final String name;
  private final String revision;
  private final String path;

  private String cacheKey = null;

  public NamespaceAndName getNamespaceAndName() {
    return new NamespaceAndName(namespace, name);
  }

  public Optional<String> getExtension() {
    return Filenames.extension(path);
  }

  public String getFilename() {
    return Filenames.filename(path);
  }

  public String getCacheKey() {
    if (cacheKey == null) {
      cacheKey = createCacheKey();
    }
    return cacheKey;
  }

  @SuppressWarnings("UnstableApiUsage")
  private String createCacheKey() {
    return Hashing.sha256()
      .hashString(revision + "/" + path, StandardCharsets.UTF_8)
      .toString();
  }

}
