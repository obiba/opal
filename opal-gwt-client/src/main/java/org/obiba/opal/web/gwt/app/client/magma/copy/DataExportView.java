/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.copy;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.*;

import java.util.Date;

import java.util.Map;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.support.PluginPackageHelper;
import org.obiba.opal.web.gwt.app.client.support.PluginPackageHelper.PluginPackageResourceCallback;
import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.database.DatabaseDto;
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
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.PluginPackagesDto;

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
  Chooser dataFormat;

  @UiField
  Panel destinationFolder;

  @UiField
  Panel idGroup;

  @UiField
  TextBox idColumn;

  @UiField
  Panel destinationDatabase;

  @UiField
  ListBox database;

  @UiField
  Panel queryPanel;

  @UiField
  CheckBox applyQuery;

  @UiField
  CodeBlock queryLabel;

  @UiField
  FlowPanel pluginsFormatContainer;

  @UiField
  FlowPanel serverFormatContainer;

  private FileSelectionPresenter.Display fileSelection;

  private JsArray<PluginPackageDto> pluginPackageDtoJsArray;

  private final EventBus eventBus;

  @Inject
  public DataExportView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.eventBus = eventBus;
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.exportData());
    initWidgets();
  }

  private void initWidgets() {
    dataFormat.addGroup(translations.fileBasedDatasources());
    dataFormat.addItemToGroup(translations.csvLabel(), ImportConfig.ImportFormat.CSV.name());
    dataFormat.addItemToGroup(translations.opalXmlLabel(), ImportConfig.ImportFormat.XML.name());
    dataFormat.addItemToGroup(translations.rSPSSLabel(), ImportConfig.ImportFormat.RSPSS.name());
    dataFormat.addItemToGroup(translations.rZSPSSLabel(), ImportConfig.ImportFormat.RZSPSS.name());
    dataFormat.addItemToGroup(translations.rSASLabel(), ImportConfig.ImportFormat.RSAS.name());
    dataFormat.addItemToGroup(translations.rXPTLabel(), ImportConfig.ImportFormat.RXPT.name());
    dataFormat.addItemToGroup(translations.rStataLabel(), ImportConfig.ImportFormat.RSTATA.name());
    dataFormat.addItemToGroup(translations.rDSLabel(), ImportConfig.ImportFormat.RDS.name());
    dataFormat.addGroup(translations.remoteServerBasedDatasources());
    dataFormat.addItemToGroup(translations.sqlLabel(), ImportConfig.ImportFormat.JDBC.name());

    pluginPackageDtoJsArray = JsArrays.create();
    ResourceRequestBuilderFactory.<PluginPackagesDto>newBuilder() //
        .forResource(UriBuilders.DS_PLUGINS.create().query("usage", "export").build()) //
        .withCallback(new PluginPackageResourceCallback(pluginPackageDtoJsArray, dataFormat))
        .get().send();

    dataFormat.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent event) {
        boolean fileBased = isFileBasedDataFormat();
        boolean fromPluginSource = isFromPluginSource();

        pluginsFormatContainer.clear();

        destinationFolder.setVisible(!fromPluginSource && fileBased);
        idGroup.setVisible(fromPluginSource || fileBased);
        destinationDatabase.setVisible(!fromPluginSource && !fileBased);
        pluginsFormatContainer.setVisible(fromPluginSource);

        if (fromPluginSource) {
          pluginsFormatContainer.setVisible(true);

          PluginPackageDto pluginPackage = PluginPackageHelper.findPluginPackage(dataFormat.getSelectedValue(), pluginPackageDtoJsArray);

          if (pluginPackage != null) {
            ResourceRequestBuilderFactory.<JavaScriptObject>newBuilder()
                .forResource(UriBuilders.DS_PLUGIN_FORM.create().query("usage", "export").build(pluginPackage.getName()))
                .withCallback(new ResourceCallback<JavaScriptObject>() {

                  @Override
                  public void onResource(Response response, JavaScriptObject resource) {
                    pluginsFormatContainer.clear();

                    JSONObject jsonSchema = new JSONObject(resource);
                    JsonSchemaGWT.buildUiIntoPanel(jsonSchema, null, pluginsFormatContainer, eventBus);
                  }
                })
                .get().send();
          }
        }

      }
    });
    destinationDatabase.setVisible(false);
    pluginsFormatContainer.setVisible(false);
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @UiHandler("submitButton")
  public void onSubmit(ClickEvent event) {
    if (isFromPluginSource()) {
      Map<HasType<ControlGroupType>, String> validateErrors = JsonSchemaGWT.validate(pluginsFormatContainer);

      if (validateErrors.entrySet().size() > 0) {
        for (Map.Entry<HasType<ControlGroupType>, String> entry: validateErrors.entrySet()) {
          modal.addAlert(entry.getValue(), AlertType.ERROR, entry.getKey());
        }
      } else {
        JSONObject model = JsonSchemaGWT.getModel(pluginsFormatContainer);
        getUiHandlers().onSubmit(getDataFormat(), model.toString(), getSelectedIdentifiersMapping(), idColumn.getText());
      }
    } else {
      getUiHandlers().onSubmit(getDataFormat(), isFileBasedDataFormat() ? getOutFile() : getSelectedDatabase(),
          getSelectedIdentifiersMapping(), idColumn.getText());
    }
  }

  private String getSelectedIdentifiersMapping() {
    return identifiers.getSelectedIndex() == 0 ? null : identifiers.getSelectedValue();
  }

  @UiHandler("applyQuery")
  public void onCheck(ClickEvent event) {
    queryLabel.getElement().setAttribute("style", applyQuery.getValue() ? "" : "opacity: 0.5;");
  }

  @Override
  public void removeFormat(ImportConfig.ImportFormat format) {
    for(int i = 0; i < dataFormat.getItemCount(); i++) {
      if(dataFormat.getValue(i).equals(format.name())) {
        dataFormat.removeItem(i);
        break;
      }
    }
  }

  @Override
  public void setDatabases(JsArray<DatabaseDto> databases) {
    database.clear();

    if(databases.length() == 0) removeFormat(ImportConfig.ImportFormat.JDBC);

    for(DatabaseDto dto : JsArrays.toIterable(databases)) {
      database.addItem(dto.getName());
    }
  }

  @Override
  public String getSelectedDatabase() {
    return database.getItemText(database.getSelectedIndex());
  }

  @Override
  public void selectIdentifiersMapping(String mapping) {
    identifiers.setSelectedValue(mapping);
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

  private boolean isFileBasedDataFormat() {
    return !isFromPluginSource() && ImportConfig.ImportFormat.valueOf(dataFormat.getSelectedValue()) != ImportConfig.ImportFormat.JDBC;
  }

  private boolean isFromPluginSource() {
    return PluginPackageHelper.findPluginPackage(dataFormat.getSelectedValue(), pluginPackageDtoJsArray) != null;
  }

  private String getOutFile() {
    Date date = new Date();
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyyMMddHHmmss");

    String suffix = "";
    if(!fileSelection.getFile().endsWith("/")) {
      suffix += "/";
    }
    suffix += fileBaseName + "-" + dateFormat.format(date);

    if(ImportConfig.ImportFormat.XML.name().equals(getDataFormat())) {
      return fileSelection.getFile() + suffix + ".zip";
    } else if(ImportConfig.ImportFormat.RSPSS.name().equals(getDataFormat())) {
      return fileSelection.getFile() + suffix + ".sav";
    } else if(ImportConfig.ImportFormat.RZSPSS.name().equals(getDataFormat())) {
      return fileSelection.getFile() + suffix + ".zsav";
    } else if(ImportConfig.ImportFormat.RSAS.name().equals(getDataFormat())) {
      return fileSelection.getFile() + suffix + ".sas7bdat";
    } else if(ImportConfig.ImportFormat.RXPT.name().equals(getDataFormat())) {
      suffix = suffix.replaceAll("-", "_");
      return fileSelection.getFile() + suffix + ".xpt";
    } else if(ImportConfig.ImportFormat.RSTATA.name().equals(getDataFormat())) {
      return fileSelection.getFile() + suffix + ".dta";
    } else if(ImportConfig.ImportFormat.RDS.name().equals(getDataFormat())) {
      return fileSelection.getFile() + suffix + ".rds";
    }

    return fileSelection.getFile() + suffix;
  }

  private String getDataFormat() {
    return dataFormat.getValue(dataFormat.getSelectedIndex());
  }

  @Override
  public void setFileWidgetDisplay(FileSelectionPresenter.Display display) {
    filePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setEnabled(true);
    fileSelection.setFieldWidth("20em");
    dataFormat.setEnabled(true);
  }

  @Override
  public void setFileBaseName(String fileBaseName) {
    this.fileBaseName = fileBaseName.replaceAll("\\.", "_");
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
