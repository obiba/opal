/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.copydata.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.magma.copydata.presenter.DataCopyUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Modal;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * View of the dialog used to export data from Opal.
 */
public class DataCopyView extends ModalPopupViewWithUiHandlers<DataCopyUiHandlers>
    implements DataCopyPresenter.Display {

  private final Translations translations;

  @UiTemplate("DataCopyView.ui.xml")
  interface Binder extends UiBinder<Widget, DataCopyView> {}

  @UiField
  Modal modal;

  @UiField
  ListBox datasources;

  @UiField
  CheckBox incremental;

  @UiField
  CheckBox copyNullValues;

  private ValidationHandler destinationValidator;

  @Inject
  public DataCopyView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;

    initWidget(uiBinder.createAndBindUi(this));

    modal.setTitle(translations.copyData());
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public String getSelectedDatasource() {
    return datasources.getValue(datasources.getSelectedIndex());
  }

  @Override
  public void setDatasources(List<DatasourceDto> datasources) {
    this.datasources.clear();
    for(DatasourceDto datasource : datasources) {
      if(!"null".equals(datasource.getType())) {
        this.datasources.addItem(datasource.getName());
      }
    }
  }

  @Override
  public boolean isIncremental() {
    return incremental.getValue();
  }

  @Override
  public boolean isCopyNullValues() {
    return copyNullValues.getValue();
  }

  @Override
  public boolean isWithVariables() {
    return true;
  }

  @Override
  public void setDestinationValidator(ValidationHandler handler) {
    destinationValidator = handler;
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

}
