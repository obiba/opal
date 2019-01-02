package org.obiba.opal.server.httpd;

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebListener;

public class OpalEnvironmentLoaderListener extends EnvironmentLoaderListener {

  @Override
  protected WebEnvironment createEnvironment(javax.servlet.ServletContext sc) {
    return super.createEnvironment(sc);
  }
}
