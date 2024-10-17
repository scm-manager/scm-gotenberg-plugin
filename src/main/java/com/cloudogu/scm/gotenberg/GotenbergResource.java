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
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

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
  @ApiResponse(
    responseCode = "502",
    description = "Unexpected Gotenberg server error"
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
