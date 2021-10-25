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

import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileObjectEnricherTest {

  @Mock
  private PdfService pdfService;

  @Mock
  private HalEnricherContext context;

  @Mock
  private HalAppender appender;

  private FileObjectEnricher enricher;

  @BeforeEach
  void createObjectUnderTest() {
    ScmPathInfoStore store = new ScmPathInfoStore();
    store.set(() -> URI.create("/"));

    enricher = new FileObjectEnricher(pdfService, Providers.of(store));
  }

  @Test
  void shouldAppendPdfLink() {
    FileObject file = new FileObject();
    file.setPath("h2g2.pdf");

    when(pdfService.isSupported(file)).thenReturn(true);
    when(context.oneRequireByType(FileObject.class)).thenReturn(file);
    NamespaceAndName namespaceAndName = new NamespaceAndName("hitchhiker", "guide");
    when(context.oneRequireByType(NamespaceAndName.class)).thenReturn(namespaceAndName);
    BrowserResult result = new BrowserResult("42", file);
    when(context.oneRequireByType(BrowserResult.class)).thenReturn(result);

    enricher.enrich(context, appender);

    verify(appender).appendLink("pdf", "/v2/gotenberg/pdf/hitchhiker/guide/42/h2g2.pdf");
  }

  @Test
  void shouldNotAppendPdfLinkIfFileIsUnsupported() {
    FileObject file = new FileObject();
    file.setPath("h2g2.mp4");

    when(pdfService.isSupported(file)).thenReturn(false);
    when(context.oneRequireByType(FileObject.class)).thenReturn(file);

    enricher.enrich(context, appender);

    verifyNoInteractions(appender);
  }

}
