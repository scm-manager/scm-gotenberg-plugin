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

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

@Extension
@Enrich(FileObject.class)
public class FileObjectEnricher implements HalEnricher {

  private final GotenbergConfigurationStore configurationStore;
  private final PdfService service;
  private final Provider<ScmPathInfoStore> pathInfoStore;

  @Inject
  public FileObjectEnricher(GotenbergConfigurationStore configurationStore, PdfService service, Provider<ScmPathInfoStore> pathInfoStore) {
    this.configurationStore = configurationStore;
    this.service = service;
    this.pathInfoStore = pathInfoStore;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    FileObject file = context.oneRequireByType(FileObject.class);
    if (configurationStore.get().isEnabled() && service.isSupported(file)) {
      NamespaceAndName repository = context.oneRequireByType(NamespaceAndName.class);
      BrowserResult browserResult = context.oneRequireByType(BrowserResult.class);


      String href = apiLinks().gotenberg().convertToPdf(
        repository.getNamespace(), repository.getName(), browserResult.getRevision(), file.getPath()
      ).asString();

      appender.appendLink("pdf", href);
    }
  }

  private RestApiLinks apiLinks() {
    return new RestApiLinks(pathInfoStore.get().get().getApiRestUri());
  }
}
