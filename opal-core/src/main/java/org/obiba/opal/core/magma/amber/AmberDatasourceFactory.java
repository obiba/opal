/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.magma.amber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import jakarta.validation.constraints.NotNull;
import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory of a datasource of the case reports or the interviews collected by Amber, extracted with an Amber API key
 * (scoped by studies, read-only).
 */
public class AmberDatasourceFactory extends AbstractDatasourceFactory {

  public static final String CASE_REPORT = "case_report";

  public static final String INTERVIEW = "interview";

  // ponytail: no export paging, a single zip with all the records and their files
  private static final String EXPORT_LIMIT = "1000000";

  private final String url;

  private final String token;

  private final String source;

  private final String study;

  private final String form;

  private String state;

  private String from;

  private String to;

  private String entityType;

  private String campaign;

  private boolean allowInvalidCertificates = false;

  private SSLSocketFactory sslSocketFactory;

  public AmberDatasourceFactory(String name, String url, String token, String source, String study, String form) {
    setName(name);
    this.url = url == null ? null : url.replaceAll("/+$", "");
    this.token = token;
    this.source = source;
    this.study = study;
    this.form = form;
  }

  public void setState(String state) {
    this.state = state;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setCampaign(String campaign) {
    this.campaign = campaign;
  }

  public void setAllowInvalidCertificates(boolean allowInvalidCertificates) {
    this.allowInvalidCertificates = allowInvalidCertificates;
  }

  public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
    this.sslSocketFactory = sslSocketFactory;
  }

  @NotNull
  @Override
  protected Datasource internalCreate() {
    if (Strings.isNullOrEmpty(url)) throw new MagmaRuntimeException("Amber url cannot be empty.");
    boolean interview = INTERVIEW.equals(source);
    try {
      String studyId = findId("study", "study", Map.of("name", study));
      String formId = findId(interview ? "interview-design" : "case-report-form", interview ? "interview design" : "case report form",
          Map.of("study", studyId, "name", form));
      Map<String, String> query = new LinkedHashMap<>();
      query.put("$limit", EXPORT_LIMIT);
      query.put("study", studyId);
      query.put(interview ? "interviewDesign" : "caseReportForm", formId);
      if (interview && !Strings.isNullOrEmpty(campaign))
        query.put("campaign", findId("campaign", "campaign", Map.of("study", studyId, "interviewDesign", formId, "name", campaign)));
      if (!Strings.isNullOrEmpty(state)) query.put("state", state);
      if (!Strings.isNullOrEmpty(from)) query.put("updatedAt[$gte]", from);
      if (!Strings.isNullOrEmpty(to)) query.put("updatedAt[$lte]", to);
      HttpURLConnection connection = open(interview ? "interview-export" : "case-report-export", query, "application/zip");
      try (InputStream zip = connection.getInputStream()) {
        return new AmberDatasource(getName(), zip, entityType);
      } finally {
        connection.disconnect();
      }
    } catch (IOException e) {
      throw new MagmaRuntimeException("Amber export failed: " + e.getMessage(), e);
    }
  }

  /**
   * The identifier of the only item with the given name.
   */
  private String findId(String service, String label, Map<String, String> criteria) throws IOException {
    Map<String, String> query = new LinkedHashMap<>(criteria);
    query.put("$limit", "1");
    HttpURLConnection connection = open(service, query, "application/json");
    try (InputStream in = connection.getInputStream()) {
      JsonNode data = new ObjectMapper().readTree(in).path("data");
      if (data.isEmpty()) throw new MagmaRuntimeException("Amber " + label + " not found: " + criteria.get("name"));
      return data.get(0).path("_id").asText();
    } finally {
      connection.disconnect();
    }
  }

  private HttpURLConnection open(String service, Map<String, String> query, String accept) throws IOException {
    String params = query.entrySet().stream()
        .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));
    HttpURLConnection connection = (HttpURLConnection) URI.create(url + "/" + service + "?" + params).toURL().openConnection();
    if (connection instanceof HttpsURLConnection https) {
      if (sslSocketFactory != null) https.setSSLSocketFactory(sslSocketFactory);
      if (allowInvalidCertificates) https.setHostnameVerifier((hostname, session) -> true);
    }
    connection.setConnectTimeout(30000);
    connection.setRequestProperty("Accept", accept);
    connection.setRequestProperty("x-api-key", token);
    int status = connection.getResponseCode();
    if (status == HttpURLConnection.HTTP_OK) return connection;
    connection.disconnect();
    if (status == HttpURLConnection.HTTP_UNAUTHORIZED)
      throw new MagmaRuntimeException("Amber API key is invalid, expired or revoked");
    if (status == HttpURLConnection.HTTP_FORBIDDEN)
      throw new MagmaRuntimeException("Amber API key does not give access to the study " + study);
    throw new MagmaRuntimeException("Amber request failed: " + service + " (" + status + ")");
  }
}
