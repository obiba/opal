/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;

/**
 *
 */
public interface OpalResources extends ClientBundle {
  public static final OpalResources INSTANCE = GWT.create(OpalResources.class);

  @NotStrict
  @Source("org/obiba/opal/web/gwt/app/public/css/opal.css")
  public CssResource css();

  @NotStrict
  @Source("org/obiba/opal/web/gwt/app/public/css/opal-blue.css")
  public CssResource cssBlue();

  @NotStrict
  @Source("org/obiba/opal/web/gwt/app/public/css/opal-mongo.css")
  public CssResource cssMongo();

}
