package org.obiba.opal.web.gwt.app.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class TaxonomiesModalPanel extends Composite {

  //  @UiTemplate("TaxonomiesModalPanel.ui.xml")
  interface TaxonomiesModalPanelUiBinder extends UiBinder<Widget, TaxonomiesModalPanel> {}

  private static final TaxonomiesModalPanelUiBinder uiBinder = GWT.create(TaxonomiesModalPanelUiBinder.class);

  @UiField
  Panel panel;

  @UiField
  HasText titleTxt;

  @UiField
  HasText descriptionTxt;

  public TaxonomiesModalPanel() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public String getTaxonomyTitle() {
    return titleTxt.getText();
  }

  public String getTaxonomyDescription() {
    return descriptionTxt.getText();
  }
}
