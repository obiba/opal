/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.obiba.opal.web.gwt.app.client.administration.database.edit.sql;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.model.client.magma.JdbcValueTableSettingsFactoryDto;

public class JdbcTableSettingsFactoryPanel extends Composite {

  interface JdbcTableSettingsFactoryUiBinder extends UiBinder<Widget, JdbcTableSettingsFactoryPanel> {
  }

  private static final JdbcTableSettingsFactoryUiBinder uiBinder = GWT.create(JdbcTableSettingsFactoryUiBinder.class);

  @UiField
  TextBox sqlTable;

  @UiField
  TextBox opalTable;

  @UiField
  TextBox entityType;

  @UiField
  TextBox tablePartitionColumn;

  @UiField
  TextBox entityIdentifierColumn;

  @UiField
  TextBox updatedTimestampColumn;

  @UiField
  TextBox whereStatement;

  @UiField
  TextBox excludedColumns;

  @UiField
  TextBox includedColumns;

  @UiField
  CheckBox multilines;

  private JdbcValueTableSettingsFactoryDto settings;

  private GroupHandler groupHandler;

  public JdbcTableSettingsFactoryPanel(String sqlTableName, GroupHandler groupHandler) {
    initWidget(uiBinder.createAndBindUi(this));
    this.groupHandler = groupHandler;
    JdbcValueTableSettingsFactoryDto settings = JdbcValueTableSettingsFactoryDto.create();
    settings.setSqlTable(sqlTableName);
    settings.setEntityType("Participant");
    setJdbcTableSettingsFactory(settings);
  }


  public JdbcTableSettingsFactoryPanel(JdbcValueTableSettingsFactoryDto settings, GroupHandler groupHandler) {
    initWidget(uiBinder.createAndBindUi(this));
    this.groupHandler = groupHandler;
    setJdbcTableSettingsFactory(settings);
  }

  @UiHandler("close")
  void onCloseButton(ClickEvent event) {
    groupHandler.onRemove(this);
  }

  @UiHandler("sqlTable")
  void onSQLTable(KeyUpEvent event) {
    groupHandler.onLabelChange(this);
  }

  @UiHandler("opalTable")
  void onOpalTable(KeyUpEvent event) {
    groupHandler.onLabelChange(this);
  }

  public String getLabel() {
    String label = sqlTable.getValue() + " (" + (tablePartitionColumn.getValue().isEmpty() ? "?" : tablePartitionColumn.getValue()) + ")";
    if (!Strings.isNullOrEmpty(opalTable.getValue()) && !Strings.isNullOrEmpty(opalTable.getValue().trim()))
      label = label + " - " + opalTable.getValue();
    return label;
  }

  public void setJdbcTableSettingsFactory(JdbcValueTableSettingsFactoryDto settings) {
    this.settings = settings;
    sqlTable.setValue(settings.getSqlTable());
    opalTable.setValue(settings.getOpalTable());
    entityType.setValue(settings.getEntityType());
    tablePartitionColumn.setValue(settings.getTablePartitionColumn());
    entityIdentifierColumn.setValue(settings.getEntityIdentifierColumn());
    updatedTimestampColumn.setValue(settings.getUpdatedTimestampColumn());
    whereStatement.setValue(settings.getEntityIdentifiersWhere());
    excludedColumns.setValue(settings.getExcludedColumns());
    includedColumns.setValue(settings.getIncludedColumns());
    multilines.setValue(settings.getMultilines());
  }

  public JdbcValueTableSettingsFactoryDto getJdbcTableSettingsFactory() {
    this.settings = JdbcValueTableSettingsFactoryDto.create();
    if (Strings.isNullOrEmpty(sqlTable.getValue())) throw new RuntimeException("SQL table name is required");
    settings.setSqlTable(sqlTable.getValue());
    settings.setOpalTable(opalTable.getValue());
    settings.setEntityType(Strings.isNullOrEmpty(entityType.getValue()) ? "Participant" : entityType.getValue());
    if (Strings.isNullOrEmpty(tablePartitionColumn.getValue())) throw new RuntimeException("Table partition column is required");
    settings.setTablePartitionColumn(tablePartitionColumn.getValue());
    settings.setEntityIdentifierColumn(entityIdentifierColumn.getValue());
    settings.setUpdatedTimestampColumn(updatedTimestampColumn.getValue());
    settings.setEntityIdentifiersWhere(whereStatement.getValue());
    settings.setExcludedColumns(excludedColumns.getValue());
    settings.setIncludedColumns(includedColumns.getValue());
    settings.setMultilines(multilines.getValue());
    return settings;
  }

  public interface GroupHandler {

    void onRemove(JdbcTableSettingsFactoryPanel settings);

    void onLabelChange(JdbcTableSettingsFactoryPanel settings);

  }

}
