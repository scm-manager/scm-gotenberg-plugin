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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.web.JsonMockHttpRequest;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({ShiroExtension.class, MockitoExtension.class})
class GotenbergResourceTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Mock
  private GotenbergConfigurationStore configurationStore;

  @Mock
  private PdfService pdfService;

  @InjectMocks
  private GotenbergResource resource;

  private final RestDispatcher dispatcher = new RestDispatcher();

  @BeforeEach
  void setUpDispatcher() {
    dispatcher.addSingletonResource(resource);
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "configuration:read:gotenberg")
  void shouldReturnConfig() throws URISyntaxException {
    GotenbergConfiguration configuration = new GotenbergConfiguration();
    configuration.setUrl("https://gotenberg.dev");
    configuration.setEnabled(true);

    when(configurationStore.get()).thenReturn(configuration);

    JsonMockHttpResponse response = invokeGetConfig();

    JsonNode node = response.getContentAsJson();
    assertThat(node.get("url").asText()).isEqualTo("https://gotenberg.dev");
    assertThat(node.get("enabled").asBoolean()).isTrue();
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "configuration:read:gotenberg")
  void shouldReturnVndContentType() throws URISyntaxException {
    when(configurationStore.get()).thenReturn(new GotenbergConfiguration());

    JsonMockHttpResponse response = invokeGetConfig();

    Object contentTypeHeader = response.getOutputHeaders().getFirst("Content-Type");
    assertThat(contentTypeHeader).hasToString("application/vnd.scmm-gotenberg-config+json;v=2");
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "configuration:read:gotenberg")
  void shouldReturnSelfLink() throws URISyntaxException {
    when(configurationStore.get()).thenReturn(new GotenbergConfiguration());

    JsonMockHttpResponse response = invokeGetConfig();

    JsonNode node = response.getContentAsJson();
    assertThat(node.get("_links").get("self").get("href").asText()).isEqualTo("/v2/gotenberg/config");
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "configuration:read:gotenberg")
  void shouldNotReturnUpdateLinkWithoutPermission() throws URISyntaxException {
    when(configurationStore.get()).thenReturn(new GotenbergConfiguration());

    JsonMockHttpResponse response = invokeGetConfig();

    JsonNode node = response.getContentAsJson();
    assertThat(node.get("_links").get("update")).isNull();
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "configuration:read,write:gotenberg")
  void shouldNotReturnUpdateLink() throws URISyntaxException {
    when(configurationStore.get()).thenReturn(new GotenbergConfiguration());

    JsonMockHttpResponse response = invokeGetConfig();

    JsonNode node = response.getContentAsJson();
    assertThat(node.get("_links").get("update").get("href").asText()).isEqualTo("/v2/gotenberg/config");
  }

  @Test
  @SubjectAware(value = "trillian")
  void shouldReturn403WithoutReadPermission() throws URISyntaxException {
    JsonMockHttpResponse response = invokeGetConfig();
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
  }

  private JsonMockHttpResponse invokeGetConfig() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/gotenberg/config");
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    dispatcher.invoke(request, response);
    return response;
  }

  @Test
  @SubjectAware(value = "trillian")
  void shouldStoreConfig() throws URISyntaxException, JsonProcessingException {
    GotenbergConfigurationDto dto = new GotenbergConfigurationDto();
    dto.setUrl("https://gotenberg.dev");
    dto.setEnabled(true);

    JsonMockHttpResponse response = invokePutConfig(dto);

    ArgumentCaptor<GotenbergConfiguration> captor = ArgumentCaptor.forClass(GotenbergConfiguration.class);
    verify(configurationStore).set(captor.capture());

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    GotenbergConfiguration configuration = captor.getValue();
    assertThat(configuration.getUrl()).isEqualTo("https://gotenberg.dev");
    assertThat(configuration.isEnabled()).isTrue();
  }

  @Test
  void shouldReturn400ForInvalidUrl() throws URISyntaxException, JsonProcessingException {
    GotenbergConfigurationDto dto = new GotenbergConfigurationDto();
    dto.setUrl("bRoKenUrl!");

    JsonMockHttpResponse response = invokePutConfig(dto);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
  }

  private JsonMockHttpResponse invokePutConfig(GotenbergConfigurationDto dto) throws URISyntaxException, JsonProcessingException {
    JsonMockHttpRequest request = JsonMockHttpRequest.put("/v2/gotenberg/config");
    request.contentType(GotenbergResource.CONTENT_TYPE);
    request.json(mapper.writeValueAsString(dto));
    JsonMockHttpResponse response = new JsonMockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  @Test
  void shouldConvertToPdf() throws IOException, URISyntaxException {
    RepositoryPath path = new RepositoryPath(
      "hitchhiker", "h2g2", "42", "praesi.pptx"
    );
    when(pdfService.getOrConvertPdf(path)).thenReturn(stream("Hello from pdf"));

    MockHttpRequest request = MockHttpRequest.get("/v2/gotenberg/pdf/hitchhiker/h2g2/42/praesi.pptx");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getContentAsString()).isEqualTo("Hello from pdf");
  }

  private InputStream stream(String content) {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }
}
