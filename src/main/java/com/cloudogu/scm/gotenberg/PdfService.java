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

import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

@Singleton
class PdfService {

  private final RepositoryManager repositoryManager;
  private final CacheFactory cacheFactory;
  private final FileResolver fileResolver;
  private final Converter converter;

  @Inject
  public PdfService(RepositoryManager repositoryManager,
                    CacheFactory cacheFactory,
                    FileResolver fileResolver,
                    Converter converter) {
    this.repositoryManager = repositoryManager;
    this.cacheFactory = cacheFactory;
    this.fileResolver = fileResolver;
    this.converter = converter;
  }

  public boolean isSupported(FileObject file) {
    return Filenames.extension(file.getPath())
      .map(converter::isConvertable)
      .orElse(false);
  }

  public InputStream getOrConvertPdf(RepositoryPath path) throws IOException {
    checkIfPathIsSupported(path);

    Repository repository = repositoryManager.get(path.getNamespaceAndName());
    if (repository == null) {
      throw notFound(entity(path.getNamespaceAndName()));
    }

    CacheFactory.Cache cache = cacheFactory.get(repository);

    Optional<InputStream> optional = cache.get(path);
    if (optional.isPresent()) {
      return optional.get();
    } else {
      return convertAndCache(cache, repository, path);
    }
  }

  private void checkIfPathIsSupported(RepositoryPath path) {
     String extension = path.getExtension().orElseThrow(() -> new FiletypeNotSupportedException(path, "files without extension are not supported"));
     if (!converter.isConvertable(extension)) {
       throw new FiletypeNotSupportedException(path, "files with %s extension are not supported", extension);
     }
  }

  private InputStream convertAndCache(CacheFactory.Cache cache, Repository repository, RepositoryPath path) throws IOException {
    try (InputStream convert = converter.convert(path, fileResolver.getContent(repository, path))) {
      cache.set(path, convert);
    }
    return cache.get(path).orElseThrow(() -> new IllegalStateException("currently cached object is not available"));
  }

}
