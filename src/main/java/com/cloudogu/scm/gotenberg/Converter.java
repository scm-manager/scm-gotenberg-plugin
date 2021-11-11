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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import sonia.scm.io.ContentTypeResolver;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
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
