/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * ListBox of Value Type of Magma (Text, integer, ...)
 */
public class ValueTypeBox extends Composite implements HasValue<String> {

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiTemplate("ValueTypeBox.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ValueTypeBox> {
  }

  @UiField
  ListBox listBox;

  private String value;

  public ValueTypeBox() {
    initWidget(uiBinder.createAndBindUi(this));
    listBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        value = listBox.getValue(listBox.getSelectedIndex());
      }
    });
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return listBox.addChangeHandler((ChangeHandler) handler);
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
    for(int i = 0; i < listBox.getItemCount(); i++) {
      String text = listBox.getItemText(i);
      if(text.equals(value)) {
        listBox.setSelectedIndex(i);
        break;
      }
    }
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    if(fireEvents) {
      setValue(value);
    } else {
      this.value = value;
    }
  }
}
