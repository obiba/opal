/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HasEnabled;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class DynamicCheckboxGroup extends Composite implements TakesValue<Set<String>>, HasEnabled {

  private Set<String> value = new HashSet<>();
  private final String key;

  private final List<CheckBox> checkBoxes;

  private boolean enabled;

  public DynamicCheckboxGroup(String key, List<JSONObject> items) {
    this.enabled = true;
    this.key = key;
    checkBoxes = new ArrayList<>();

    FlowPanel panel = new FlowPanel();

    if (items != null) {
      for(JSONObject item : items) {
        String itemKey = item.isString().stringValue();
        String itemTitle = item.containsKey("title") ? item.get("title").isString().stringValue() : itemKey;
        CheckBox checkBox = new CheckBox(itemKey);
        checkBox.setName(getFormKey());
        checkBox.setFormValue(itemKey);

        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
          @Override
          public void onValueChange(ValueChangeEvent<Boolean> event) {
            CheckBox source = (CheckBox) event.getSource();
            Boolean value = event.getValue();

            String formValue = source.getFormValue();

            if (value != null && value) {
              addItem(formValue);
            } else {
              removeItem(formValue);
            }
          }
        });

        checkBoxes.add(checkBox);
        checkBox.setText(itemTitle);

        FlowPanel checkBoxPanel = new FlowPanel();
        checkBoxPanel.getElement().addClassName("checkbox");
        checkBoxPanel.add(checkBox);

        panel.add(checkBoxPanel);
      }
    }

    initWidget(panel);
  }

  public String getKey() {
    return key;
  }
  public String getFormKey() {
    return key.endsWith("[]") ? key : key + "[]";
  }

  @Override
  public void setValue(Set<String> value) {
    this.value = value;

    for(CheckBox checkBox : checkBoxes) {
      checkBox.setValue(value.contains(checkBox.getFormValue()), false);
    }
  }

  @Override
  public Set<String> getValue() {
    return value;
  }

  public void addItem(String item) {
    value.add(item);
  }

  public void removeItem(String item) {
    value.remove(item);
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;

    for (CheckBox button : checkBoxes) {
      button.setEnabled(enabled);
    }
  }
}
