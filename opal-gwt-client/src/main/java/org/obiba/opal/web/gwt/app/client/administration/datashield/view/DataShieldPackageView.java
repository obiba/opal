/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.view;

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackagePresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.workbench.view.PropertiesTable;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.model.client.opal.r.EntryDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class DataShieldPackageView extends PopupViewImpl implements DataShieldPackagePresenter.Display {

  @UiTemplate("DataShieldPackageView.ui.xml")
  interface DataShieldPackageViewUiBinder extends UiBinder<DialogBox, DataShieldPackageView> {}

  private static DataShieldPackageViewUiBinder uiBinder = GWT.create(DataShieldPackageViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  DialogBox dialog;

  @UiField
  DockLayoutPanel contentLayout;

  @UiField
  ResizeHandle resizeHandle;

  @UiField
  Button closeButton;

  @UiField
  PropertiesTable properties;

//  @UiField
//  Label name;

//  @UiField
//  Label packageName;
//
//  @UiField
//  Label version;
//
//  @UiField
//  Label title;
//
//  @UiField
//  Label author;
//
//  @UiField
//  Label maintainer;
//
//  @UiField
//  Label depends;
//
//  @UiField
//  Label description;
//
//  @UiField
//  Label license;
//
//  @UiField
//  Label opalVersion;
//
//  @UiField
//  Anchor url;
//
//  @UiField
//  Anchor bugReports;

  //
  // Constructors
  //

  @Inject
  public DataShieldPackageView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWidgets();
  }

  private void initWidgets() {
    dialog.hide();
    resizeHandle.makeResizable(contentLayout);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected PopupPanel asPopupPanel() {
    return dialog;
  }

  @Override
  public void show() {
    dialog.setText(translations.dataShieldPackageDescription());
    super.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public HasClickHandlers getCloseButton() {
    return closeButton;
  }

  @Override
  public void addProperty(EntryDto dto) {
    if(dto.getValue().toLowerCase().startsWith("http")) {
      Anchor a = new Anchor();
      a.setTarget("_blank");
      a.setHref(dto.getValue());
      a.setText(dto.getValue());
      properties.addProperty(new Label(dto.getKey()), a.asWidget());
    } else {
      properties.addProperty(dto.getKey(), dto.getValue());
    }
  }

  @Override
  public void clearProperties() {
    properties.clearProperties();
    properties.setBordered(false);
    properties.setCondensed(true);
    properties.setZebra(true);
    properties.setKeyStyleNames("span3 small-indent");
    properties.addStyleName("small-dual-indent");
    properties.addStyleName("top-margin");
  }
}
