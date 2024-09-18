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

import java.util.Locale;
import java.util.Optional;

class Filenames {

  private Filenames() {
  }

  public static String filename(String originalPath) {
    String path = originalPath;
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    int index = path.lastIndexOf('/');
    if (index > 0) {
      return path.substring(index + 1);
    }
    return path;
  }

  public static Optional<String> extension(String path) {
    int index = path.lastIndexOf('.');
    if (index > 0 && index + 1 < path.length()) {
      return Optional.of(path.substring(index + 1).toLowerCase(Locale.ENGLISH));
    }
    return Optional.empty();
  }
}
