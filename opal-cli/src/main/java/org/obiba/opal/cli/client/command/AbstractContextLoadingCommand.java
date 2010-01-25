/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 */
public abstract class AbstractContextLoadingCommand<T> extends AbstractCommand<T> {

  protected static final String DEFAULT_CONTEXT_PATH = "classpath:/spring/opal-cli/context.xml";

  private ConfigurableApplicationContext context = null;

  public void execute() {
    try {
      context = loadContext();
      afterContextLoaded();
    } finally {
      if(context != null) {
        context.close();
      }
    }
  }

  protected void afterContextLoaded() {
    executeWithContext();
  }

  protected abstract void executeWithContext();

  @SuppressWarnings("unchecked")
  protected <E> E getBean(String name) {
    return (E) context.getBean(name);
  }

  protected ConfigurableApplicationContext loadContext() {
    return new ClassPathXmlApplicationContext(DEFAULT_CONTEXT_PATH);
  }

}
