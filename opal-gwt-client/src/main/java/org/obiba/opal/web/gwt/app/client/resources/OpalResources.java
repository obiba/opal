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

import com.github.gwtbootstrap.client.ui.resources.Resources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.TextResource;

/**
 *
 */
public interface OpalResources extends Resources {

  OpalResources INSTANCE = GWT.create(OpalResources.class);

  @NotStrict
  @Source("org/obiba/opal/web/gwt/app/public/css/opal.css")
  CssResource css();

  @Override
  @Source("org/obiba/opal/web/gwt/app/public/css/bootstrap.min.css")
  TextResource bootstrapCss();

}
