package org.obiba.opal.web.gwt.rest.client;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.http.client.URL;

public class UriBuilder {

  private final List<String> path;

  private final List<String> query;

  private UriBuilder() {
    path = Lists.newArrayList();
    query = Lists.newArrayList();
  }

  public static UriBuilder create() {
    return new UriBuilder();
  }

  /**
   * Append segments element(s) to path
   * <p/>
   * usage:<br/>
   * path("name","myDatasource","name","myTable"); <br/>
   * append /name/myDatasource/name/myTable to path
   *
   * @param segments
   * @return
   */
  @SuppressWarnings("ConstantConditions")
  public UriBuilder segment(String... segments) {
    Preconditions.checkArgument(segments != null);
    for(String segment : segments) {
      Preconditions.checkArgument(segment != null);
    }
    append(path, segments);
    return this;
  }

  /**
   * Append argument element(s) to path
   * <p/>
   * usage:<br/>
   * appendQuery("var1","val1","var2","val2"); <br/>
   * append ?var1=val1&var2=val2 at the end of path
   *
   * @param queryItems
   * @return
   */
  @SuppressWarnings("ConstantConditions")
  public UriBuilder query(String... queryItems) {
    Preconditions.checkArgument(queryItems != null);
    Preconditions.checkArgument(queryItems.length % 2 == 0);
    for(int i = 0; i < queryItems.length; i += 2) {
      Preconditions.checkArgument(queryItems[i] != null);
    }
    append(query, queryItems);
    return this;
  }

  /**
   * Return url
   *
   * @return
   */
  public String build(String... args) {
    StringBuilder sb = new StringBuilder();
    int idx = 0;
    for(String segment : path) {
      if(segment.contains("%7B") && segment.contains("%7D")) {
        String substring = segment.substring(segment.indexOf("%7B") + 3, segment.indexOf("%7D"));
        if(idx > args.length) {
          throw new IllegalArgumentException("Missing argument");
        }
        segment = segment.replace("%7B" + substring + "%7D", URL.encodePathSegment(args[idx++]));
      }
      sb.append("/").append(segment);
    }
    for(int i = 0; i < query.size(); i += 2) {
      if(i == 0) sb.append("?");
      sb.append(query.get(i)).append("=").append(query.get(i + 1));
      if(i + 2 < query.size()) sb.append("&");
    }
    return sb.toString();
  }

  /**
   * Parses a path to return a UriBuilder
   *
   * @param path
   * @return uriBuilder
   */
  public UriBuilder fromPath(String p) {
    String cleanedPath = p.startsWith("/") ? p.substring(1) : p;
    append(path, cleanedPath.split("/"));
    return this;
  }

  private void append(Collection<String> list, String... strings) {
    for(String pathItem : strings) {
      list.add(URL.encodePathSegment(pathItem));
    }
  }

  public static final UriBuilder URI_REPORT_TEMPLATES = create().segment("report-templates");

  public static final UriBuilder URI_PROJECT = create().segment("project", "{}");

  public static final UriBuilder URI_PROJECT_SUMMARY = create().segment("project", "{}", "summary");

  public static final UriBuilder URI_PROJECT_REPORT_TEMPLATES = create().segment("project", "{}", "report-templates");

  public static final UriBuilder URI_DATASOURCE = create().segment("datasource", "{}");

  public static final UriBuilder URI_DATASOURCE_LOCALES = create().segment("datasource", "{}", "locales");

  public static final UriBuilder URI_DATASOURCE_TABLES = create().segment("datasource", "{}", "tables");

  public static final UriBuilder URI_DATASOURCE_TABLE = create().segment("datasource", "{}", "table", "{}");

  public static final UriBuilder URI_DATASOURCE_TABLE_INDEX = create()
      .segment("datasource", "{}", "table", "{}", "index");

  public static final UriBuilder URI_DATASOURCE_TABLE_VARIABLES = create()
      .segment("datasource", "{}", "table", "{}", "variables");

  public static final UriBuilder URI_DATASOURCE_TABLE_VARIABLE = create()
      .segment("datasource", "{}", "table", "{}", "variable", "{}");

  public static final UriBuilder URI_DATASOURCE_TABLE_LOCALES = create()
      .segment("datasource", "{}", "table", "{}", "locales");

}
