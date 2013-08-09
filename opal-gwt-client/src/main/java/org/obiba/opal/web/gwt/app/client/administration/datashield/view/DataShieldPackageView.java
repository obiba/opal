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
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.model.client.opal.EntryDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class DataShieldPackageView extends ModalPopupViewWithUiHandlers<ModalUiHandlers> implements DataShieldPackagePresenter.Display {

  interface DataShieldPackageViewUiBinder extends UiBinder<Widget, DataShieldPackageView> {}

  private static final DataShieldPackageViewUiBinder uiBinder = GWT.create(DataShieldPackageViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private static int DIALOG_HEIGHT = 400;

  private static final int DIALOG_WIDTH = 480;

  private final Widget widget;

  @UiField
  Modal dialog;

  @UiField
  PropertiesTable properties;

  //
  // Constructors
  //

  @Inject
  public DataShieldPackageView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    dialog.setMinHeight(DIALOG_WIDTH);
    dialog.setMinWidth(DIALOG_HEIGHT);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void show() {
    dialog.setTitle(translations.dataShieldPackageDescription());
    super.show();
  }

  @UiHandler("closeButton")
  public void onCloseButton(ClickEvent event) {
    dialog.hide();
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
