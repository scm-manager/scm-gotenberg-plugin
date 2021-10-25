/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cloudogu.scm.gotenberg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.io.ContentType;
import sonia.scm.io.ContentTypeResolver;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConverterTest {

  @Mock
  private AdvancedHttpClient client;

  @Mock
  private ContentTypeResolver contentTypeResolver;

  private Converter converter;

  @Nested
  class IsConvertableTests {

    @BeforeEach
    void setUpObjectUnderTest() {
      converter = new Converter(client, contentTypeResolver);
    }

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

    @BeforeEach
    void setUpObjectUnderTest() {
      converter = new Converter(client, contentTypeResolver, () -> "+++");
    }

    @Test
    void shouldSendConvertRequest() throws IOException {
      InputStream content = new ByteArrayInputStream("Don't Panic".getBytes(StandardCharsets.UTF_8));
      RepositoryPath path = new RepositoryPath(
        "hitchhiker", "h2g2", "42", "a/b/c/h2g2.pdf"
      );

      when(contentTypeResolver.resolve("a/b/c/h2g2.pdf")).thenReturn(new PDF());
      when(client.post("http://localhost:3000/forms/libreoffice/convert")).thenReturn(request);
      when(request.request()).thenReturn(response);

      converter.convert(content, path);

      ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
      verify(request).contentType("multipart/form-data; boundary=+++");
      verify(request).rawContent(captor.capture());

      String expected = String.join(System.getProperty("line.separator"),
        "--+++",
        "Content-Disposition: form-data; name=\"files\"; filename=\"h2g2.pdf\"",
        "Content-Type: application/pdf",
        "Content-Transfer-Encoding: binary",
        "",
        "Don't Panic",
        "--+++--",
        ""
      );

      String captured = new String(captor.getValue(), StandardCharsets.UTF_8);
      assertThat(captured).isEqualTo(expected);
    }

  }

  private static class PDF implements ContentType {

    @Override
    public String getPrimary() {
      return "application";
    }

    @Override
    public String getSecondary() {
      return "pdf";
    }

    @Override
    public String getRaw() {
      return "application/pdf";
    }

    @Override
    public boolean isText() {
      return false;
    }

    @Override
    public Optional<String> getLanguage() {
      return Optional.empty();
    }
  }

}
