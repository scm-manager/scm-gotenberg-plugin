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

import sonia.scm.BadRequestException;
import sonia.scm.ContextEntry;

import java.util.List;

@SuppressWarnings("java:S110") // deep inheritance is ok for exceptions
class FiletypeNotSupportedException extends BadRequestException {

  FiletypeNotSupportedException(RepositoryPath path, String message, Object... args) {
    super(context(path), String.format(message, args));
  }

  private static List<ContextEntry> context(RepositoryPath path) {
    return ContextEntry.ContextBuilder.entity("path", path.getPath())
      .in("revision", path.getRevision())
      .in(path.getNamespaceAndName())
      .build();
  }

  @Override
  public String getCode() {
    return "3NSmqNTZj1";
  }
}
