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
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

@Singleton
class PdfService {

  private static final String STORE = "gotenberg";

  private final RepositoryManager repositoryManager;
  private final BlobStoreFactory blobStoreFactory;
  private final FileResolver fileResolver;
  private final Converter converter;

  @Inject
  public PdfService(RepositoryManager repositoryManager,
                    BlobStoreFactory blobStoreFactory,
                    FileResolver fileResolver,
                    Converter converter) {
    this.repositoryManager = repositoryManager;
    this.blobStoreFactory = blobStoreFactory;
    this.fileResolver = fileResolver;
    this.converter = converter;
  }

  public boolean isSupported(FileObject file) {
    return Filenames.extension(file.getPath())
      .map(converter::isConvertable)
      .orElse(false);
  }

  public synchronized InputStream getOrConvertPdf(RepositoryPath path) throws IOException {
    checkIfPathIsSupported(path);

    Repository repository = repositoryManager.get(path.getNamespaceAndName());
    if (repository == null) {
      throw notFound(entity(path.getNamespaceAndName()));
    }

    BlobStore cache = store(repository);

    Optional<Blob> optional = cache.getOptional(path.getCacheKey());
    if (optional.isPresent()) {
      return optional.get().getInputStream();
    } else {
      return convertAndCache(cache, repository, path);
    }
  }

  private BlobStore store(Repository repository) {
    return blobStoreFactory.withName(STORE).forRepository(repository).build();
  }

  private void checkIfPathIsSupported(RepositoryPath path) {
     String extension = path.getExtension().orElseThrow(() -> new FiletypeNotSupportedException(path, "files without extension are not supported"));
     if (!converter.isConvertable(extension)) {
       throw new FiletypeNotSupportedException(path, "files with %s extension are not supported", extension);
     }
  }

  private InputStream convertAndCache(BlobStore cache, Repository repository, RepositoryPath path) throws IOException {
    InputStream convert = converter.convert(path, fileResolver.getContent(repository, path));
    Blob blob = cache(cache, path, convert);
    return blob.getInputStream();
  }

  private Blob cache(BlobStore cache, RepositoryPath path, InputStream content) throws IOException {
    Blob blob = cache.create(path.getCacheKey());
    try (OutputStream output = blob.getOutputStream()) {
      ByteStreams.copy(content, output);
      blob.commit();
    }
    return blob;
  }

}
