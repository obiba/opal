package org.obiba.opal.core.service.impl;

import org.easymock.EasyMock;
import org.junit.Before;
import org.obiba.opal.core.service.SubjectAclService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(classes = DefaultDatabaseRegistryTest.Config.class)
public class DefaultDatabaseRegistryTest extends AbstractJUnit4SpringContextTests {

  @Before
  public void setUp() {

  }

  @Configuration
  public static class Config extends AbstractOrientDbTestConfig {

    @Bean
    public SubjectAclService subjectAclService() {
      return EasyMock.createMock(SubjectAclService.class);
    }

  }
}
