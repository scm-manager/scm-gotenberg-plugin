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

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.net.ahc.FormContentBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConverterTest {

  @Mock
  private GotenbergConfigurationStore configurationStore;

  @Mock
  private AdvancedHttpClient client;

  @InjectMocks
  private Converter converter;


  @Nested
  class IsConvertableTests {

    @ParameterizedTest
    @ValueSource(strings = {"docx", "doc", "odt", "ppt", "pptx"})
    void shouldReturnTrue(String extension) {
      assertThat(converter.isConvertable(extension)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"mp4", "mkv", "ogg", "mp3", "webp"})
    void shouldReturnFalse(String extension) {
      assertThat(converter.isConvertable(extension)).isFalse();
    }

  }

  @Nested
  class ConvertTests {

    @Mock(answer = Answers.RETURNS_SELF)
    private AdvancedHttpRequestWithBody request;

    @Mock
    private AdvancedHttpResponse response;

    @Mock(answer = Answers.RETURNS_SELF)
    private FormContentBuilder formContentBuilder;

    @Test
    void shouldSendConvertRequest() throws IOException {
      GotenbergConfiguration configuration = new GotenbergConfiguration();
      configuration.setUrl("https://gotenberg.dev");
      when(configurationStore.get()).thenReturn(configuration);

      InputStream content = new ByteArrayInputStream("Don't Panic".getBytes(StandardCharsets.UTF_8));
      RepositoryPath path = new RepositoryPath("hitchhiker", "h2g2", "42", "a/b/c/h2g2.pdf");

      when(client.post("https://gotenberg.dev/forms/libreoffice/convert")).thenReturn(request);
      when(request.formContent()).thenReturn(formContentBuilder);
      when(formContentBuilder.build()).thenReturn(request);
      when(request.request()).thenReturn(response);

      when(response.isSuccessful()).thenReturn(true);

      converter.convert(path, content);

      verify(formContentBuilder).file("files", "h2g2.pdf", content);
      verify(request).request();
      verify(response).contentAsStream();
    }

    @Test
    void shouldThrowGotenbergServerException() throws IOException {
      GotenbergConfiguration configuration = new GotenbergConfiguration();
      configuration.setUrl("https://gotenberg.dev");
      when(configurationStore.get()).thenReturn(configuration);

      InputStream content = new ByteArrayInputStream("Don't Panic".getBytes(StandardCharsets.UTF_8));
      RepositoryPath path = new RepositoryPath("hitchhiker", "h2g2", "42", "a/b/c/h2g2.pdf");

      when(client.post("https://gotenberg.dev/forms/libreoffice/convert")).thenReturn(request);
      when(request.formContent()).thenReturn(formContentBuilder);
      when(formContentBuilder.build()).thenReturn(request);
      when(request.request()).thenReturn(response);

      when(response.getStatus()).thenReturn(Response.Status.BAD_GATEWAY.getStatusCode());
      when(response.isSuccessful()).thenReturn(false);

      Exception exception = assertThrows(GotenbergServerException.class, () -> converter.convert(path, content));

      String expectedMessage = "Unexpected response by Gotenberg server: 502";
      assertEquals(expectedMessage, exception.getMessage());

    }

  }
}
