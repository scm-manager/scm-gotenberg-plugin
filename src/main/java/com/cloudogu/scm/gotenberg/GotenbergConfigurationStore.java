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

import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GotenbergConfigurationStore {

  private static final String NAME = "gotenberg";

  private final ConfigurationStore<GotenbergConfiguration> store;

  @Inject
  public GotenbergConfigurationStore(ConfigurationStoreFactory factory) {
    this.store = factory.withType(GotenbergConfiguration.class).withName(NAME).build();
  }

  public GotenbergConfiguration get() {
    return store.getOptional().orElse(new GotenbergConfiguration());
  }

  public void set(GotenbergConfiguration configuration) {
    Permissions.write().check();
    store.set(configuration);
  }

}
