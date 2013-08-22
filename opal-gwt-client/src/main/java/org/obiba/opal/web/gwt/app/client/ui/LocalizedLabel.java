package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.Label;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class LocalizedLabel extends Composite {

  interface LocalizedLabelUiBinder extends UiBinder<Widget, LocalizedLabel> {}

  private static final LocalizedLabelUiBinder uiBinder = GWT.create(LocalizedLabelUiBinder.class);
//
//  @UiField
//  InlineLabel title;

  @UiField
  Label locale;

  @UiField
  InlineLabel text;

  public LocalizedLabel() {
    initWidget(uiBinder.createAndBindUi(this));
  }

//  public void setWidgetTitle(String title) {
//    this.title.setText(title);
//  }

  public void setLocale(String locale) {
    this.locale.setText(locale);
  }

  public void setText(String text) {
    this.text.setText(text);
  }

}
