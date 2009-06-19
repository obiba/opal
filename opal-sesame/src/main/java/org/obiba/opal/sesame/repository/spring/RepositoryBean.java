/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.repository.spring;

import org.openrdf.repository.base.RepositoryWrapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Manages the life-cycle of an OpenRdf {@code Repository} within a Spring {@code ApplicationContext}. <p/>
 * Specifically, the class wraps a {@code Repository} and implements both {@code InitializingBean} and
 * {@code DisposableBean} to manage the delegate's life-cycle: calling {@code Repository#initialize()} and
 * {@code Repository#shutDown()}.
 */
public class RepositoryBean extends RepositoryWrapper implements InitializingBean, DisposableBean {

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(getDelegate(), "delegate is mandatory");
    getDelegate().initialize();
  }

  public void destroy() throws Exception {
    getDelegate().shutDown();
  }
}
