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

import com.cloudogu.jaxrstie.GenerateLinkBuilder;
import com.google.common.io.ByteStreams;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;

@Path("v2/gotenberg")
@GenerateLinkBuilder(className = "RestApiLinks")
public class GotenbergResource {

  private final PdfService pdfService;

  @Inject
  public GotenbergResource(PdfService pdfService) {
    this.pdfService = pdfService;
  }

  @GET
  @Produces("application/pdf")
  @Path("pdf/{namespace}/{name}/{revision}/{path: .*}")
  public StreamingOutput convertToPdf(@PathParam("namespace") String namespace,
                                      @PathParam("name") String name,
                                      @PathParam("revision") String revision,
                                      @PathParam("path") String path) {

    RepositoryPath repositoryPath = new RepositoryPath(namespace, name, revision, path);

    return os -> {
      InputStream pdf = pdfService.getOrConvertPdf(repositoryPath);
      ByteStreams.copy(pdf, os);
      os.close();
    };
  }

}
