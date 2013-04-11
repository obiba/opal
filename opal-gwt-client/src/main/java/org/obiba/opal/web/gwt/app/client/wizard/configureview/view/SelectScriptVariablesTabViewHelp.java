/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class SelectScriptVariablesTabViewHelp extends Composite {
  //
  // Static Variables
  //

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  //
  // Constructors
  //

  public SelectScriptVariablesTabViewHelp() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("SelectScriptVariablesTabViewHelp.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, SelectScriptVariablesTabViewHelp> {}
}
