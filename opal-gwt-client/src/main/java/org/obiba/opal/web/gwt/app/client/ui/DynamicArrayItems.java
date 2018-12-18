package org.obiba.opal.web.gwt.app.client.ui;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DynamicArrayItems extends Composite implements TakesValue<Set<String>>, HasEnabled {

  private final String key;
  private final String type;

  private List<TextBox> inputs;
  private FlowPanel inputsPanel;

  private Button plusButton;

  private boolean enabled;

  public String getKey() {
    return key;
  }

  public String getFormKey() {
    return key.endsWith("[]") ? key : key + "[]";
  }

  public String getType() {
    return type;
  }

  public DynamicArrayItems(String key, String type) {
    this.enabled = true;
    this.key = key;
    this.type = type;
    inputs = new ArrayList<>();
    inputsPanel = new FlowPanel();

    FlowPanel panel = new FlowPanel();
    panel.add(inputsPanel);

    panel.add((addPlusButton()));

    initWidget(panel);
  }

  @Override
  public void setValue(Set<String> ts) {
    for (String val : ts) {
      addInput(val);
    }
  }

  @Override
  public Set<String> getValue() {
    Set<String> values = new HashSet<>();

    for (TextBox input : inputs) {
      String inputValue = input.getValue();
      if (inputValue != null && inputValue.trim().length() > 0) values.add(inputValue);
    }

    return values;
  }

  private FlowPanel addPlusButton() {
    FlowPanel panel = new FlowPanel();

    Button button = new Button("<i class='icon-plus'></i>");
    button.addStyleName("btn btn-info");

    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        addInput("");
      }
    });

    plusButton = button;
    panel.add(button);

    return panel;
  }

  private void addInput(String initValue) {
    FlowPanel inputPanel = new FlowPanel();
    if (enabled) inputPanel.getElement().addClassName("input-append");
    inputPanel.getElement().setAttribute("style", "display: block;");
    TextBox textBox = new TextBox();

    textBox.setValue(initValue);
    textBox.setName(getFormKey());

    textBox.addAttachHandler(new Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        if (!event.isAttached() && event.getSource() instanceof TextBox) {
          inputs.remove(event.getSource());
        }
      }
    });

    textBox.setEnabled(enabled);
    inputPanel.add(textBox);

    Button removeButton = new Button("<i class='icon-remove'></i>");
    removeButton.setVisible(enabled);
    removeButton.getElement().addClassName("btn");

    removeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Button source = (Button) event.getSource();
        FlowPanel parent = (FlowPanel) source.getParent();
        parent.removeFromParent();
      }
    });

    removeButton.setEnabled(enabled);
    inputPanel.add(removeButton);
    inputs.add(textBox);
    inputsPanel.add(inputPanel);
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;

    plusButton.setEnabled(enabled);
    plusButton.setVisible(enabled);

    for (Widget inputPanel : inputsPanel) {
      if (inputPanel instanceof FlowPanel) {
        if (!enabled) inputPanel.getElement().removeClassName("input-append");

        for (Widget widget: (FlowPanel) inputPanel) {
          if (widget instanceof HasEnabled) ((HasEnabled) widget).setEnabled(enabled);
          if (widget instanceof Button) widget.setVisible(enabled);
        }
      }
    }
  }
}
