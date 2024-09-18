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

import org.apache.shiro.authz.AuthorizationException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(ShiroExtension.class)
class GotenbergConfigurationStoreTest {

  private GotenbergConfigurationStore store;

  @BeforeEach
  void setUpObjectUnderTest() {
    store = new GotenbergConfigurationStore(new InMemoryConfigurationStoreFactory());
  }

  @Test
  void shouldReturnInitialConfiguration() {
    GotenbergConfiguration configuration = store.get();
    assertThat(configuration.getUrl()).isEqualTo("http://localhost:3000");
    assertThat(configuration.isEnabled()).isFalse();
  }

  @Test
  @SubjectAware("trillian")
  void shouldFailWithoutWritePermission() {
    GotenbergConfiguration configuration = store.get();
    configuration.setEnabled(true);

    assertThrows(AuthorizationException.class, () -> store.set(configuration));
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "configuration:write:gotenberg")
  void shouldStoreConfiguration() {
    GotenbergConfiguration configuration = store.get();
    configuration.setEnabled(true);
    store.set(configuration);

    assertThat(store.get().isEnabled()).isTrue();
  }

}
