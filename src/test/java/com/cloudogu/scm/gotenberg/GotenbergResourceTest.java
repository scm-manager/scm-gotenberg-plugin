package com.cloudogu.scm.gotenberg;

import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.web.RestDispatcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GotenbergResourceTest {

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
