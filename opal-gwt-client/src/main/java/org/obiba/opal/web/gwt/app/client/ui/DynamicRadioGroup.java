package org.obiba.opal.web.gwt.app.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;

public class DynamicRadioGroup extends Composite implements TakesValue<String> {

  private String value;

  private List<RadioButton> radios = new ArrayList<>();

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return value;
  }

  public DynamicRadioGroup(List<String> items) {
    FlowPanel panel = new FlowPanel();

    if (items != null) {
      for(String item: items) {
        RadioButton radioButton = new RadioButton(item);
        radioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
          @Override
          public void onValueChange(ValueChangeEvent<Boolean> event) {
            RadioButton source = (RadioButton) event.getSource();
            Boolean value = event.getValue();

            if (value != null && value) {
              setValue(source.getName());

              for(RadioButton radio : radios) {
                if (!radio.getName().equals(source.getName())) {
                  radio.setValue(false, false);
                }
              }
            }
          }
        });

        radios.add(radioButton);
        panel.add(radioButton);
      }
    }

    initWidget(panel);
  }
}
