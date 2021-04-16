/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Allow static access to ApplicationContext.
 * But be sure it is initialized before you access it!!
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

  @SuppressWarnings("StaticNonFinalField")
  private static ApplicationContext applicationContext;

  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  @SuppressWarnings({ "AccessStaticViaInstance", "AssignmentToStaticFieldFromInstanceMethod" })
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
}
