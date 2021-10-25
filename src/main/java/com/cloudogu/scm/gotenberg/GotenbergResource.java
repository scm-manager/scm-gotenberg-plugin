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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;

@Path("v2/gotenberg")
@GenerateLinkBuilder(className = "RestApiLinks")
@OpenAPIDefinition(tags = {
  @Tag(name = "Gotenberg", description = "Gotenberg plugin related endpoints")
})
public class GotenbergResource {

  @VisibleForTesting
  static final String CONTENT_TYPE = VndMediaType.PREFIX + "gotenberg-config" + VndMediaType.SUFFIX;

  private final PdfService pdfService;
  private final GotenbergConfigurationStore store;

  @Inject
  public GotenbergResource(PdfService pdfService, GotenbergConfigurationStore store) {
    this.pdfService = pdfService;
    this.store = store;
  }

  @GET
  @Path("config")
  @Produces(CONTENT_TYPE)
  @Operation(
    summary = "Get configuration",
    description = "Returns gotenberg configuration",
    tags = "Gotenberg",
    operationId = "gotenberg_get_config"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = CONTENT_TYPE,
      schema = @Schema(implementation = GotenbergConfigurationDto.class)
    )
  )
  @ApiResponse(
    responseCode = "403",
    description = "Missing permission to read gotenberg configuration",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public GotenbergConfigurationDto getConfig(@Context UriInfo uriInfo) {
    Permissions.read().check();
    return GotenbergConfigurationDto.from(store.get(), createLinks(uriInfo));
  }

  private Links createLinks(UriInfo uriInfo) {
    String self = uriInfo.getAbsolutePath().toASCIIString();
    Links.Builder links = Links.linkingTo();
    links.self(self);
    if (Permissions.write().isPermitted()) {
      links.single(Link.link("update", self));
    }
    return links.build();
  }

  @PUT
  @Path("config")
  @Consumes(CONTENT_TYPE)
  @Operation(
    summary = "Change configuration",
    description = "Change gotenberg configuration",
    tags = "Gotenberg",
    operationId = "gotenberg_set_config"
  )
  @ApiResponse(
    responseCode = "204",
    description = "success"
  )
  @ApiResponse(
    responseCode = "403",
    description = "Missing permission to write gotenberg configuration",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response setConfig(@Valid GotenbergConfigurationDto dto) {
    store.set(dto.toEntity());
    return Response.noContent().build();
  }

  @GET
  @Produces("application/pdf")
  @Path("pdf/{namespace}/{name}/{revision}/{path: .*}")
  @Operation(
    summary = "Convert to pdf",
    description = "Converts the given document to a pdf",
    tags = "Gotenberg",
    operationId = "gotenberg_convert_to_pdf"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success"
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
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
