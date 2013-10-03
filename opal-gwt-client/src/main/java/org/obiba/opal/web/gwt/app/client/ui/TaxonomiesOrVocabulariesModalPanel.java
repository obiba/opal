package org.obiba.opal.web.gwt.app.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class TaxonomiesOrVocabulariesModalPanel extends Composite {

  interface TaxonomiesOrVocabulariesModalUiBinder extends UiBinder<Widget, TaxonomiesOrVocabulariesModalPanel> {}

  private static final TaxonomiesOrVocabulariesModalUiBinder uiBinder = GWT
      .create(TaxonomiesOrVocabulariesModalUiBinder.class);

  @UiField
  Panel panel;

  @UiField
  HasText titleTxt;

  @UiField
  HasText descriptionTxt;

  public TaxonomiesOrVocabulariesModalPanel() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public String getTitleTxt() {
    return titleTxt.getText();
  }

  public String getDescriptionTxt() {
    return descriptionTxt.getText();
  }

  public void setTitleTxt(String title) {
    titleTxt.setText(title);
  }

  public void setDescriptionTxt(String description) {
    descriptionTxt.setText(description);
  }
}
