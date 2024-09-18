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

import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class GotenbergConfigurationTest {

  @Test
  void marshalAndUnmarshal() {
    GotenbergConfiguration configuration = new GotenbergConfiguration();
    configuration.setUrl("https://gotenberg.dev");
    configuration.setEnabled(true);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JAXB.marshal(configuration, out);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    configuration = JAXB.unmarshal(in, GotenbergConfiguration.class);

    assertThat(configuration.getUrl()).isEqualTo("https://gotenberg.dev");
    assertThat(configuration.isEnabled()).isTrue();
  }

}
