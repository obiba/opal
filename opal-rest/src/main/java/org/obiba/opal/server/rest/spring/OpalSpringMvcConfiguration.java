/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.spring;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.eclipse.jetty.servlet.ServletHolder;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.server.httpd.OpalJettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

import com.thoughtworks.xstream.XStream;

//@Configuration
public class OpalSpringMvcConfiguration {

  @Autowired
  private OpalJettyServer jettyServer;

  @PostConstruct
  public void addDispatcherServlet() {
    DispatcherServlet dispatch = new DispatcherServlet();
    dispatch.setContextAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    dispatch.setPublishContext(false);
    jettyServer.getContext().addServlet(new ServletHolder(dispatch), "/mvc/*");
  }

  @Bean
  public HandlerMapping getHandlerMapping() {
    DefaultAnnotationHandlerMapping mapping = new DefaultAnnotationHandlerMapping();
    mapping.setUseDefaultSuffixPattern(false);
    return mapping;
  }

  @Bean
  public AnnotationMethodHandlerAdapter getHandlerAdpater() {
    AnnotationMethodHandlerAdapter adapter = new AnnotationMethodHandlerAdapter();
    HttpMessageConverter<?>[] converters = adapter.getMessageConverters();
    HttpMessageConverter<?>[] newConverters = Arrays.copyOf(converters, converters.length + 1);
    newConverters[newConverters.length - 1] = new MarshallingHttpMessageConverter(new XStreamMarshaller() {
      @Override
      public XStream getXStream() {
        return MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
      }
    });
    adapter.setMessageConverters(newConverters);

    adapter.setWebBindingInitializer(getBindingInit());
    return adapter;
  }

  @Bean
  public ConfigurableWebBindingInitializer getBindingInit() {
    ConfigurableWebBindingInitializer init = new ConfigurableWebBindingInitializer();
    init.setConversionService(new FormattingConversionService());
    return init;
  }

}
