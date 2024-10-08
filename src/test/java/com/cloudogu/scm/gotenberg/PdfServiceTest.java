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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryBlobStoreFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfServiceTest {

  @Mock
  private RepositoryManager repositoryManager;

  @Mock
  private FileResolver fileResolver;

  @Mock
  private Converter converter;

  private PdfService pdfService;

  @BeforeEach
  void setUpObjectUnderTest() {
    pdfService = new PdfService(
      repositoryManager,
      new CacheFactory(new InMemoryBlobStoreFactory()),
      fileResolver,
      converter
    );
  }

  @Nested
  class IsSupportedTests {

    @Test
    void shouldReturnTrue() {
      when(converter.isConvertable("docx")).thenReturn(true);
      assertThat(pdfService.isSupported(fileObject("a/b/c/my.docx"))).isTrue();
    }

    @Test
    void shouldReturnFalseWithoutExtension() {
      assertThat(pdfService.isSupported(fileObject("Dockerfile"))).isFalse();
    }

    @Test
    void shouldReturnFalseForUnsupportedExtension() {
      when(converter.isConvertable("mkv")).thenReturn(false);
      assertThat(pdfService.isSupported(fileObject("video.mkv"))).isFalse();
    }

    private FileObject fileObject(String path) {
      FileObject fileObject = new FileObject();
      fileObject.setPath(path);
      return fileObject;
    }

  }

  @Nested
  class GetOrConvertPdfTests {

    private final Repository repository = RepositoryTestData.createHeartOfGold();

    @Test
    void shouldFailForPathWithoutExtension() {
      RepositoryPath path = path("Dockerfile");
      assertThrows(FiletypeNotSupportedException.class, () -> pdfService.getOrConvertPdf(path));
    }

    @Test
    void shouldFailForPathWithUnsupportedExtension() {
      when(converter.isConvertable("mkv")).thenReturn(false);

      RepositoryPath path = path("video.mkv");
      assertThrows(FiletypeNotSupportedException.class, () -> pdfService.getOrConvertPdf(path));
    }

    @Test
    void shouldFailWithNonExistingRepository() {
      when(converter.isConvertable("pptx")).thenReturn(true);
      RepositoryPath path = path("praesi.pptx");
      assertThrows(NotFoundException.class, () -> pdfService.getOrConvertPdf(path));
    }

    @Test
    void shouldConvert() throws IOException {
      RepositoryPath path = path("praesi.pptx");
      mockConversion(path);

      InputStream content = pdfService.getOrConvertPdf(path);

      assertThat(content).hasContent("Hello from pdf");
    }

    private void mockConversion(RepositoryPath path) throws IOException {
      String ext = path.getExtension().orElseThrow(() -> new IllegalStateException("extension is required"));
      InputStream pptx = stream("Hello from " + ext);
      InputStream pdf = stream("Hello from pdf");

      when(converter.isConvertable(ext)).thenReturn(true);
      when(repositoryManager.get(path.getNamespaceAndName())).thenReturn(repository);
      when(fileResolver.getContent(repository, path)).thenReturn(pptx);
      when(converter.convert(path, pptx)).thenReturn(pdf);
    }

    @Test
    void shouldUseCache() throws IOException {
      RepositoryPath path = path("praesi.pptx");
      mockConversion(path);

      pdfService.getOrConvertPdf(path);
      // read from cache, no conversion of resolving required
      verifyNoMoreInteractions(converter);
      verifyNoMoreInteractions(fileResolver);

      InputStream content = pdfService.getOrConvertPdf(path);
      assertThat(content).hasContent("Hello from pdf");
    }

    private ByteArrayInputStream stream(String content) {
      return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private RepositoryPath path(String path) {
      return new RepositoryPath(repository.getNamespace(), repository.getName(), "42", path);
    }

  }

}
