package org.obiba.opal.web.ws.cfg;

import javax.annotation.PostConstruct;

import org.obiba.runtime.upgrade.VersionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.wordnik.swagger.jaxrs.config.BeanConfig;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;

@Component
public class SwaggerConfig {

  @Value("${org.obiba.opal.public.url}")
  private String url;

  @Autowired
  private VersionProvider versionProvider;

  @PostConstruct
  public void initSwagger() {
    BeanConfig config = new BeanConfig();
    config.setResourcePackage("org.obiba.opal");
    config.setScan(true);
    config.setVersion(versionProvider.getVersion().toString());
    config.setBasePath(url + OpalWsConfig.WS_ROOT);
    config.setTitle("Opal REST API");
  }

  @Bean
  public ApiListingResourceJSON apiListingResourceJSON() {
    return new ApiListingResourceJSON();
  }

  @Bean
  public ApiDeclarationProvider apiDeclarationProvider() {
    return new ApiDeclarationProvider();
  }

  @Bean
  public ResourceListingProvider resourceListingProvider() {
    return new ResourceListingProvider();
  }

}
