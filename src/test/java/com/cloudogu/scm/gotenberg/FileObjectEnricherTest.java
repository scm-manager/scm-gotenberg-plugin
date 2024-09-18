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

  @Mock
  private GotenbergConfigurationStore configurationStore;

  private FileObjectEnricher enricher;

  @BeforeEach
  void createObjectUnderTest() {
    ScmPathInfoStore store = new ScmPathInfoStore();
    store.set(() -> URI.create("/"));

    enricher = new FileObjectEnricher(configurationStore, pdfService, Providers.of(store));
  }

  @Test
  void shouldAppendPdfLink() {

    FileObject file = new FileObject();
    file.setPath("h2g2.pdf");

    GotenbergConfiguration configuration = new GotenbergConfiguration();
    configuration.setEnabled(true);

    when(configurationStore.get()).thenReturn(configuration);
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

    GotenbergConfiguration configuration = new GotenbergConfiguration();
    configuration.setEnabled(true);

    when(configurationStore.get()).thenReturn(configuration);
    when(pdfService.isSupported(file)).thenReturn(false);
    when(context.oneRequireByType(FileObject.class)).thenReturn(file);

    enricher.enrich(context, appender);

    verifyNoInteractions(appender);
  }

  @Test
  void shouldNotAppendPdfLinkIfGotenbergIsDisabled() {
    FileObject file = new FileObject();
    file.setPath("h2g2.mp4");

    GotenbergConfiguration configuration = new GotenbergConfiguration();
    configuration.setEnabled(false);

    when(configurationStore.get()).thenReturn(configuration);
    when(context.oneRequireByType(FileObject.class)).thenReturn(file);

    enricher.enrich(context, appender);

    verifyNoInteractions(appender);
  }

}
