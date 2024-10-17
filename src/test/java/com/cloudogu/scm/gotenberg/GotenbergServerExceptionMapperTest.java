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


import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GotenbergServerExceptionMapperTest {

  @Test
  void shouldMapErrorToResponse() {
    GotenbergServerException exception = new GotenbergServerException("error");
    GotenbergServerExceptionMapper mapper = new GotenbergServerExceptionMapper();

    try (Response response = mapper.toResponse(exception)) {
      assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_GATEWAY.getStatusCode());
      assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);
    }
  }

}
