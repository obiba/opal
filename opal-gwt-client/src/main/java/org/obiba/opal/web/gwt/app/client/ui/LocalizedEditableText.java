package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.InputAddOn;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

public class LocalizedEditableText extends Composite implements TakesValue<LocalizedEditableText.LocalizedText> {

  interface LocalizedEditableTextUiBinder extends UiBinder<Widget, LocalizedEditableText> {}

  private static final LocalizedEditableTextUiBinder uiBinder = GWT.create(LocalizedEditableTextUiBinder.class);

  private LocalizedText localizedText;

  private boolean largeText;

  @UiField
  InputAddOn locale;

  @UiField
  TextArea textArea;

  @UiField
  TextBox textBox;

  public LocalizedEditableText() {
    initWidget(uiBinder.createAndBindUi(this));
    setLargeText(false);
  }

  public void setLargeText(boolean large) {
    largeText = large;
    textArea.setVisible(large);
    textBox.setVisible(!large);
  }

  @Override
  public void setValue(LocalizedText value) {
    localizedText = value;
    locale.setPrependText(value.getLocale());
    textBox.setText(value.getText());
    textArea.setText(value.getText());
  }

  @Override
  public LocalizedText getValue() {
    return localizedText;
  }

  public HasText getTextBox() {
    return largeText ? textArea : textBox;
  }

  public static class LocalizedText {

    private String locale;

    private String text;

    public LocalizedText(String locale, String text) {
      this.locale = locale;
      this.text = text;
    }

    public String getLocale() {
      return locale;
    }

    public void setLocale(String locale) {
      this.locale = locale;
    }

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }

  }
}
