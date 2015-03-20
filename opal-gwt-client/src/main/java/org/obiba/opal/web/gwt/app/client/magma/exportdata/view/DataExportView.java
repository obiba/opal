/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.exportdata.view;

import java.util.Date;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter.DataExportUiHandlers;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.identifiers.IdentifiersMappingDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.CodeBlock;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * View of the dialog used to export data from Opal.
 */
public class DataExportView extends ModalPopupViewWithUiHandlers<DataExportUiHandlers>
    implements DataExportPresenter.Display {

  private final Translations translations;

  private String fileBaseName;

  interface Binder extends UiBinder<Widget, DataExportView> {}

  @UiField
  Modal modal;

  @UiField
  Alert exportNTable;

  @UiField
  Panel identifiersPanel;

  @UiField
  Chooser identifiers;

  @UiField
  SimplePanel filePanel;

  @UiField
  Chooser fileFormat;

  @UiField
  Panel queryPanel;

  @UiField
  CheckBox applyQuery;

  @UiField
  CodeBlock queryLabel;

  private FileSelectionPresenter.Display fileSelection;

  @Inject
  public DataExportView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.exportData());
    initWidgets();
  }

  private void initWidgets() {
    fileFormat.addItemToGroup(translations.csvLabel(), ImportConfig.ImportFormat.CSV.name());
    fileFormat.addItemToGroup(translations.opalXmlLabel(), ImportConfig.ImportFormat.XML.name());
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @UiHandler("submitButton")
  public void onSubmit(ClickEvent event) {
    getUiHandlers().onSubmit(getFileFormat(), getOutFile(), getSelectedIdentifiersMapping());
  }

  private String getSelectedIdentifiersMapping() {
    return identifiers.getSelectedIndex() == 0 ? null : identifiers.getSelectedValue();
  }

  @UiHandler("applyQuery")
  public void onCheck(ClickEvent event) {
    queryLabel.getElement().setAttribute("style", applyQuery.getValue() ? "" : "opacity: 0.5;");
  }

  @Override
  public void setValuesQuery(String query) {
    queryPanel.setVisible(!Strings.isNullOrEmpty(query) && !"*".equals(query));
    applyQuery.setValue(queryPanel.isVisible());
    queryLabel.setText(query);
  }

  @Override
  public boolean applyQuery() {
    return applyQuery.getValue();
  }

  @Override
  public void setIdentifiersMappings(JsArray<IdentifiersMappingDto> mappings) {
    identifiers.clear();
    identifiers.addItem(translations.opalDefaultIdentifiersLabel());
    for(int i = 0; i < mappings.length(); i++) {
      identifiers.addItem(mappings.get(i).getName());
    }
    identifiers.setSelectedIndex(0);
    identifiersPanel.setVisible(mappings.length() > 0);
  }

  private String getOutFile() {
    Date date = new Date();
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyyMMddHHmmss");

    String suffix = "";
    if(!fileSelection.getFile().endsWith("/")) {
      suffix += "/";
    }
    suffix += fileBaseName + "-" + dateFormat.format(date);

    if("xml".equalsIgnoreCase(getFileFormat())) {
      return fileSelection.getFile() + suffix + ".zip";
    }

    return fileSelection.getFile() + suffix;
  }

  private String getFileFormat() {
    return fileFormat.getValue(fileFormat.getSelectedIndex());
  }

  @Override
  public void setFileWidgetDisplay(FileSelectionPresenter.Display display) {
    filePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setEnabled(true);
    fileSelection.setFieldWidth("20em");
    fileFormat.setEnabled(true);
  }

  @Override
  public void setFileBaseName(String fileBaseName) {
    this.fileBaseName = fileBaseName;
  }

  @Override
  public void showExportNAlert(String message) {
    exportNTable.setText(message);
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }
}
