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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileResolverTest {

  @Mock
  private RepositoryServiceFactory repositoryServiceFactory;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService repositoryService;

  @InjectMocks
  private FileResolver resolver;

  @Test
  void shouldReturnContent() throws IOException {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    RepositoryPath path = new RepositoryPath(
      heartOfGold.getNamespace(), heartOfGold.getName(), "42", "h2g2.pdf"
    );

    InputStream stream = new ByteArrayInputStream(new byte[]{});

    when(repositoryServiceFactory.create(heartOfGold)).thenReturn(repositoryService);
    when(repositoryService.getCatCommand().setRevision("42").getStream("h2g2.pdf")).thenReturn(stream);

    InputStream content = resolver.getContent(heartOfGold, path);

    assertThat(content).isSameAs(stream);
  }

}
