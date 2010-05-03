/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest;

import org.restlet.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 *
 */
public class OpalApplicationFactory {

  @Autowired
  private ApplicationContext applicationContext;

  public OpalApplication createApplication(Context context) {
    OpalApplication opalApplication = new OpalApplication(context);
    applicationContext.getAutowireCapableBeanFactory().autowireBean(opalApplication);
    return opalApplication;
  }

}
