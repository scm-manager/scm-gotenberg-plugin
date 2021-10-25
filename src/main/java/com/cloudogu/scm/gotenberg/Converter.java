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
import com.google.common.io.ByteStreams;
import sonia.scm.io.ContentTypeResolver;
import sonia.scm.net.ahc.AdvancedHttpClient;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Supplier;

public class Converter {

  private static final Set<String> CONVERTABLE = ImmutableSet.of(
    "bib", "doc", "xml", "docx", "fodt", "html", "ltx", "txt", "odt", "ott", "pdb", "pdf", "psw", "rtf", "sdw", "stw",
    "sxw", "uot", "vor", "wps", "epub", "png", "bmp", "emf", "eps", "fodg", "gif", "jpg", "met", "odd", "otg", "pbm",
    "pct", "pgm", "ppm", "ras", "std", "svg", "svm", "swf", "sxd", "sxw", "tiff", "xhtml", "xpm", "fodp", "potm", "pot",
    "pptx", "pps", "ppt", "pwp", "sda", "sdd", "sti", "sxi", "uop", "wmf", "csv", "dbf", "dif", "fods", "ods", "ots",
    "pxl", "sdc", "slk", "stc", "sxc", "uos", "xls", "xlt", "xlsx", "tif", "jpeg", "odp"
  );


  private final AdvancedHttpClient client;
  private final ContentTypeResolver contentTypeResolver;
  private final Supplier<String> boundaryGenerator;

  @Inject
  public Converter(AdvancedHttpClient client, ContentTypeResolver contentTypeResolver) {
    this(client, contentTypeResolver, () -> "------------------------" + System.currentTimeMillis() );
  }

  @VisibleForTesting
  Converter(AdvancedHttpClient client, ContentTypeResolver contentTypeResolver, Supplier<String> boundaryGenerator) {
    this.client = client;
    this.contentTypeResolver = contentTypeResolver;
    this.boundaryGenerator = boundaryGenerator;
  }

  public boolean isConvertable(String extension) {
    return CONVERTABLE.contains(extension);
  }

  public InputStream convert(InputStream content, RepositoryPath path) throws IOException {
    String boundary = boundaryGenerator.get();

    return client.post("http://localhost:3000/forms/libreoffice/convert")
      .contentType("multipart/form-data; boundary=" + boundary)
      .rawContent(createContent(content, path, boundary))
      .request()
      .contentAsStream();
  }

  private byte[] createContent(InputStream content, RepositoryPath path, String boundary) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8), true)) {
      writer.append("--").println(boundary);
      writer.append("Content-Disposition: form-data; name=\"files\"; filename=\"").append(path.getFilename()).println("\"");
      writer.append("Content-Type: ").println(contentTypeResolver.resolve(path.getPath()).getRaw());
      writer.println("Content-Transfer-Encoding: binary");
      writer.println();

      writer.flush();

      ByteStreams.copy(content, baos);

      writer.println();
      writer.append("--").append(boundary).println("--");
      writer.flush();
    }

    return baos.toByteArray();
  }

}
