/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.resource.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.opal.spi.resource.Resource;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class DefaultResource implements Resource, Serializable {

  private String name;

  private String format;

  private String uri;

  private String identity;

  private String secret;

  public DefaultResource() {
  }

  public DefaultResource(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  @Override
  public URI toURI() throws URISyntaxException {
    return new URI(uri);
  }

  @Override
  public Credentials getCredentials() {
    return new Credentials() {
      @Override
      public String getIdentity() {
        return identity;
      }

      @Override
      public String getSecret() {
        return secret;
      }
    };
  }

  public static Builder newResource(String name) {
    return new Builder(name);
  }

  public static class Builder {

    private final DefaultResource resource;

    private String scheme;

    private String host;

    private Integer port;

    private List<String> segments;

    private Map<String, String> query;

    public Builder(String name) {
      this.resource = new DefaultResource(name);
    }

    public Builder name(String name) {
      resource.setName(name);
      return this;
    }

    public Builder format(String format) {
      resource.setFormat(format);
      return this;
    }

    public Builder scheme(String scheme) {
      this.scheme = scheme;
      return this;
    }

    public Builder host(String host) {
      this.host = host;
      return this;
    }

    public Builder port(int port) {
      this.port = port;
      return this;
    }

    public Builder segments(String... path) {
      this.segments = Lists.newArrayList(path);
      return this;
    }

    public Builder path(String path) {
      this.segments = Lists.newArrayList();
      for (String s : path.split("/")) {
        if (!Strings.isNullOrEmpty(s))
          this.segments.add(s);
      }
      return this;
    }

    public Builder query(String key, String value) {
      if (this.query == null)
        this.query = Maps.newLinkedHashMap();
      this.query.put(key, value);
      return this;
    }

    public Builder uri(String uri) {
      resource.uri = uri;
      return this;
    }

    public Builder credentials(String identity, String secret) {
      resource.identity = identity;
      resource.secret = secret;
      return this;
    }

    public DefaultResource build() {
      if (Strings.isNullOrEmpty(resource.uri))
        resource.uri = toURI();
      return resource;
    }

    // TODO encoding
    private String toURI() {
      StringBuilder builder = new StringBuilder();
      if (!Strings.isNullOrEmpty(scheme))
        builder.append(scheme).append("://");
      if (!Strings.isNullOrEmpty(host)) {
        builder.append(host);
        if (port != null && port > 0)
          builder.append(":").append(port);
      }
      if (segments != null)
        segments.forEach(s -> builder.append("/").append(s));
      if (query != null && !query.isEmpty()) {
        builder.append("?");
        builder.append(query.keySet().stream()
            .map(k -> k + "=" + query.get(k))
            .reduce((x, y) -> x + "&" + y).get());
      }
      return builder.toString();
    }

  }
}
