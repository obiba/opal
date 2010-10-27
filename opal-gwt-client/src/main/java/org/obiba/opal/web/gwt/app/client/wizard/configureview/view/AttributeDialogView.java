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
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;

public class AttributeDialogView extends Composite {

  @UiTemplate("AttributeDialogView.ui.xml")
  interface MyUiBinder extends UiBinder<DialogBox, AttributeDialogView> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  RadioButton nameDropdownRadioChoice;

  @UiField
  RadioButton nameFieldRadioChoice;

  @UiField
  ListBox labels;

  @UiField
  TextBox attributeName;

  @UiField
  ScrollPanel inputFieldPanel;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  public AttributeDialogView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    dialog.show();
  }
}
