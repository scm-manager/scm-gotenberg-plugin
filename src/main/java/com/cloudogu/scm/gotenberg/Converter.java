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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import sonia.scm.io.ContentTypeResolver;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.util.HttpUtil;

import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.Supplier;

public final class Converter {

  private static final Set<String> CONVERTABLE = ImmutableSet.of(
    "bib", "doc", "xml", "docx", "fodt", "html", "ltx", "txt", "odt", "ott", "pdb", "pdf", "psw", "rtf", "sdw", "stw",
    "sxw", "uot", "vor", "wps", "epub", "png", "bmp", "emf", "eps", "fodg", "gif", "jpg", "met", "odd", "otg", "pbm",
    "pct", "pgm", "ppm", "ras", "std", "svg", "svm", "swf", "sxd", "sxw", "tiff", "xhtml", "xpm", "fodp", "potm", "pot",
    "pptx", "pps", "ppt", "pwp", "sda", "sdd", "sti", "sxi", "uop", "wmf", "csv", "dbf", "dif", "fods", "ods", "ots",
    "pxl", "sdc", "slk", "stc", "sxc", "uos", "xls", "xlt", "xlsx", "tif", "jpeg", "odp"
  );

  private final GotenbergConfigurationStore configurationStore;
  private final AdvancedHttpClient client;

  @Inject
  public Converter(GotenbergConfigurationStore configurationStore, AdvancedHttpClient client) {
    this.configurationStore = configurationStore;
    this.client = client;
  }

  @VisibleForTesting
  Converter(GotenbergConfigurationStore configurationStore, AdvancedHttpClient client, ContentTypeResolver contentTypeResolver, Supplier<String> boundaryGenerator) {
    this.configurationStore = configurationStore;
    this.client = client;
  }

  public boolean isConvertable(String extension) {
    return CONVERTABLE.contains(extension);
  }

  public InputStream convert(RepositoryPath path, InputStream content) throws IOException {
    return client.post(createConvertUrl())
      .formContent()
      .file("files", path.getFilename(), content)
      .build()
      .request()
      .contentAsStream();
  }

  private String createConvertUrl() {
    return HttpUtil.append(configurationStore.get().getUrl(), "/forms/libreoffice/convert");
  }
}
