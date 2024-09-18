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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FilenamesTest {

  @Nested
  class FilenameTests {

    @Test
    void shouldReturnFilename() {
      assertThat(Filenames.filename("App.java")).isEqualTo("App.java");
      assertThat(Filenames.filename("a/b/c/Main.java")).isEqualTo("Main.java");
    }

    @Test
    void shouldRemoveEndingSlash() {
      assertThat(Filenames.filename("a/b/c/App.java/")).isEqualTo("App.java");
    }

  }

  @Nested
  class ExtensionTests {

    @Test
    void shouldReturnExtension() {
      assertThat(Filenames.extension("test.txt")).contains("txt");
      assertThat(Filenames.extension("a/b/c/Main.java")).contains("java");
    }

    @Test
    void shouldReturnExtensionToLowerCase() {
      assertThat(Filenames.extension("test.TxT")).contains("txt");
      assertThat(Filenames.extension("App.JAVA")).contains("java");
    }

    @Test
    void shouldReturnEmptyOptional() {
      assertThat(Filenames.extension("Dockerfile")).isEmpty();
      assertThat(Filenames.extension(".hiddenfile")).isEmpty();
      assertThat(Filenames.extension("strange.")).isEmpty();
    }

  }

}
