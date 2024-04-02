/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rock;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.io.ByteStreams;
import org.obiba.opal.core.domain.AppCredentials;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.service.AbstractRServerSession;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

class RockSession extends AbstractRServerSession implements RServerSession, RServerConnection {

  private static final Logger log = LoggerFactory.getLogger(RockSession.class);

  private final App app;

  private final AppCredentials credentials;

  private String rockSessionId;

  protected RockSession(String serverName, App app, AppCredentials credentials, String user, TransactionalThreadFactory transactionalThreadFactory, EventBus eventBus) throws RServerException {
    super(serverName, UUID.randomUUID().toString(), user, transactionalThreadFactory, eventBus);
    this.app = app;
    this.credentials = credentials;
    openSession();
  }

  //
  // Connection methods
  //

  @Override
  public void assignData(String symbol, String content) throws RServerException {
    touch();
    String serverUrl = getRSessionResourceUrl("/_assign");
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = createHeaders();
    headers.setContentType(MediaType.valueOf("application/x-rdata"));
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl)
        .queryParam("s", symbol);

    try {
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(content, headers), String.class);
    } catch (RestClientException e) {
      throw new RockServerException("Assign R data failed", e);
    }
  }

  @Override
  public void assignScript(String symbol, String content) throws RServerException {
    touch();
    String serverUrl = getRSessionResourceUrl("/_assign");
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = createHeaders();
    headers.setContentType(MediaType.valueOf("application/x-rscript"));
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl)
        .queryParam("s", symbol);

    try {
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(content, headers), String.class);
    } catch (RestClientException e) {
      throw new RockServerException("Assign R expression failed", e);
    }
  }

  @Override
  public RServerResult eval(String expr, RSerialize serialize) throws RServerException {
    touch();
    long start = System.currentTimeMillis();
    String serverUrl = getRSessionResourceUrl("/_eval");
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = createHeaders();
    headers.setContentType(MediaType.valueOf("application/x-rscript"));

    try {
      if (RSerialize.RAW == serialize) {
        // accept application/octet-stream
        headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON));
        ResponseEntity<byte[]> response = restTemplate.exchange(serverUrl, HttpMethod.POST, new HttpEntity<>(expr, headers), byte[].class);
        log.debug("eval: {}ms", calculateDuration(start));
        MDC.put("r_size", String.format("%s", response.getBody() == null ? 0 : response.getBody().length));
        return new RockResult(response.getBody());
      } else {
        // accept application/json
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        ResponseEntity<String> response = restTemplate.exchange(serverUrl, HttpMethod.POST, new HttpEntity<>(expr, headers), String.class);
        String jsonSource = response.getBody();
        log.trace("R expr: {}", expr);
        log.trace("JSON result: {}", jsonSource);
        RServerResult rval = new RockResult(jsonSource);
        log.debug("eval: {}ms", calculateDuration(start));
        MDC.put("r_size", String.format("%s", response.getBody() == null ? 0 : jsonSource.getBytes().length));
        return rval;
      }
    } catch (RestClientException e) {
      calculateDuration(start);
      throw new RockServerException("Error while evaluating '" + expr + "'", e);
    }
  }

  @Override
  public void writeFile(String fileName, InputStream in) throws RServerException {
    touch();
    long start = System.currentTimeMillis();
    try {
      HttpHeaders headers = createHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("file", new MultiPartInputStreamResource(in, fileName));
      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      String serverUrl = getRSessionResourceUrl("/_upload");
      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl)
          .queryParam("path", fileName)
          .queryParam("overwrite", true);

      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> response = restTemplate.postForEntity(builder.toUriString(), requestEntity, String.class);
      log.debug("write file: {}ms", calculateDuration(start));
      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error("File upload to {} failed: {}", serverUrl, response.getStatusCode());
        throw new RockServerException("File upload failed: " + response.getStatusCode());
      }
    } catch (RestClientException e) {
      calculateDuration(start);
      throw new RockServerException("File upload failed", e);
    }
  }

  @Override
  public void readFile(String fileName, OutputStream out) throws RServerException {
    touch();
    long start = System.currentTimeMillis();
    try {
      HttpHeaders headers = createHeaders();
      headers.setAccept(Collections.singletonList(MediaType.ALL));

      String serverUrl = getRSessionResourceUrl("/_download");
      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl)
          .queryParam("path", fileName);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.execute(builder.build().toUri(), HttpMethod.GET,
          request -> request.getHeaders().putAll(headers),
          (ResponseExtractor<Void>) response -> {
            calculateDuration(start);
            if (!response.getStatusCode().is2xxSuccessful()) {
              log.error("File download from {} failed: {}", serverUrl, response.getStatusCode());
              throw new RRuntimeException("File download failed: " + response.getStatusCode());
            } else {
              ByteStreams.copy(response.getBody(), out);
              out.flush();
              out.close();
            }
            return null;
          });
    } catch (RestClientException e) {
      calculateDuration(start);
      throw new RockServerException("File download failed", e);
    }
  }

  @Override
  public String getLastError() {
    return null;
  }

  //
  // Session methods
  //

  @Override
  public void close() {
    if (isClosed()) return;
    super.close();
    closeSession();
  }

  @Override
  public boolean isClosed() {
    return Strings.isNullOrEmpty(rockSessionId);
  }

  @Override
  public synchronized void execute(ROperation rop) {
    touch();
    lock.lock();
    setBusy(true);
    touch();
    try {
      rop.doWithConnection(this);
    } finally {
      setBusy(false);
      touch();
      lock.unlock();
    }
  }

  //
  // Private methods
  //

  private long calculateDuration(long start) {
    long elapsed = System.currentTimeMillis() - start;
    String durationStr = MDC.get("r_duration");
    if (Strings.isNullOrEmpty(durationStr)) {
      MDC.put("r_duration", elapsed + "");
    } else {
      long duration = Long.parseLong(durationStr) + elapsed;
      MDC.put("r_duration", duration + "");
    }
    return elapsed;
  }

  private void openSession() throws RServerException {
    long start = System.currentTimeMillis();
    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<RockSessionInfo> response = restTemplate.exchange(getRSessionsResourceUrl(), HttpMethod.POST, new HttpEntity<>(createHeaders()), RockSessionInfo.class);
      RockSessionInfo info = response.getBody();
      this.rockSessionId = info.getId();
    } catch (RestClientException e) {
      throw new RockServerException("Failure when opening a Rock R session", e);
    } finally {
      calculateDuration(start);
    }
  }

  private RockSessionInfo getSession() throws RServerException {
    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<RockSessionInfo> response = restTemplate.exchange(getRSessionResourceUrl(""), HttpMethod.GET, new HttpEntity<>(createHeaders()), RockSessionInfo.class);
      return response.getBody();
    } catch (RestClientException e) {
      throw new RockServerException("Failure when accessing a Rock R session", e);
    }
  }

  private void closeSession() {
    long start = System.currentTimeMillis();
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.exchange(getRSessionResourceUrl(""), HttpMethod.DELETE, new HttpEntity<>(createHeaders()), Void.class);
      this.rockSessionId = null;
    } catch (RestClientException e) {
      String msg = "Failure when closing the Rock R session {}";
      if (log.isDebugEnabled())
        log.warn(msg, rockSessionId, e);
      else
        log.warn(msg, rockSessionId);
    } finally {
      calculateDuration(start);
    }
  }

  private String getRSessionsResourceUrl() {
    return String.format("%s/r/sessions", app.getServer());
  }

  private String getRSessionResourceUrl(String path) {
    return String.format("%s/r/session/%s%s", app.getServer(), rockSessionId, path);
  }

  private HttpHeaders createHeaders() {
    return new HttpHeaders() {{
      String auth = credentials.getUser() + ":" + credentials.getPassword();
      byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
      String authHeader = "Basic " + new String(encodedAuth);
      add("Authorization", authHeader);
    }};
  }

  private static class MultiPartInputStreamResource extends InputStreamResource {

    private final String fileName;

    public MultiPartInputStreamResource(InputStream inputStream, String fileName) {
      super(inputStream, fileName);
      this.fileName = fileName;
    }

    @Override
    public String getFilename() {
      return fileName;
    }

    @Override
    public long contentLength() {
      return -1; // we do not want to generally read the whole stream into memory ...
    }
  }
}
