package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.InputAddOn;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LocalizedEditableText extends Composite implements TakesValue<LocalizedEditableText.LocalizedText> {

  interface LocalizedEditableTextUiBinder extends UiBinder<Widget, LocalizedEditableText> {}

  private static final LocalizedEditableTextUiBinder uiBinder = GWT.create(LocalizedEditableTextUiBinder.class);

  private LocalizedText localizedText;

  @UiField
  InputAddOn locale;

  @UiField
  TextBox text;

  public LocalizedEditableText() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setValue(LocalizedText value) {
    localizedText = value;
    locale.setPrependText(value.getLocale());
    text.setText(value.getText());
  }

  @Override
  public LocalizedText getValue() {
    return localizedText;
  }

  public TextBox getTextBox() {
    return text;
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
