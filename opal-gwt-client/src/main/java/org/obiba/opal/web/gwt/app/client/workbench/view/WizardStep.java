/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 *
 */
public class WizardStep extends FlowPanel {

  private Label title;

  public WizardStep() {
    this("");
  }

  public WizardStep(String title) {
    super();
    addStyleName("step");
    add(this.title = new Label(title));
    this.title.addStyleName("title");
  }

  public void setStepTitle(String text) {
    title.setText(text);
  }

  public void removeStepContent() {
    for(int i = 1; i < getChildren().size(); i++) {
      remove(i);
    }
  }

}
