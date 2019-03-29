package org.obiba.opal.server.httpd;

import io.buji.pac4j.filter.SecurityFilter;
import org.obiba.opal.pac4j.Pac4jConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PostConstruct;

@Component("opalSecurityFilter")
public class OpalSecurityFilter extends SecurityFilter {

  @Autowired
  private Pac4jConfigurer pac4jConfigurer;

  @PostConstruct
  public void init() {
    setConfig(pac4jConfigurer.getConfig());
  }

  public static class Wrapper extends DelegatingFilterProxy {
    public Wrapper() {
      super("opalSecurityFilter");
    }
  }

}
