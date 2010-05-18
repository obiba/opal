/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.ws.cfg;

import java.net.URL;

import org.jboss.resteasy.plugins.server.servlet.ConfigurationBootstrap;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResteasyDeploymentConfiguration {

  // http://jira.springframework.org/browse/SPR-7167
  private ResteasyDeployment deployment;

  @Bean
  public ResteasyDeployment resteasyDeployment() {
    if(deployment == null) {
      ConfigurationBootstrap restEasyBootstrap = new ConfigurationBootstrap() {

        @Override
        public String getParameter(String name) {
          return null;
        }

        @Override
        public URL[] getScanningUrls() {
          return null;
        }

      };
      deployment = restEasyBootstrap.createDeployment();
      deployment.start();
    }
    return deployment;
  }

  @Bean
  public SpringBeanProcessor resteasyPostProcessor() {
    return new SpringBeanProcessor(resteasyDeployment().getDispatcher());
  }

}
