/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.database.edit.sql;

import com.github.gwtbootstrap.client.ui.Accordion;
import com.github.gwtbootstrap.client.ui.AccordionGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.JdbcValueTableSettingsFactoryDto;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JdbcTableSettingsFactoriesEditor extends Composite {

  interface JdbcTableSettingsFactoriesEditorUiBinder extends UiBinder<Widget, JdbcTableSettingsFactoriesEditor> {}

  private static final JdbcTableSettingsFactoriesEditorUiBinder uiBinder = GWT.create(JdbcTableSettingsFactoriesEditorUiBinder.class);

  @UiField
  TextBox newSettings;

  @UiField
  Accordion accordion;

  private List<JdbcTableSettingsFactoryPanel> settingsList;

  public JdbcTableSettingsFactoriesEditor() {
    initWidget(uiBinder.createAndBindUi(this));
    this.settingsList = Lists.newArrayList();
  }

  @UiHandler("addSettings")
  void onAddButton(ClickEvent event) {
    addJdbcTableSettings();
  }

  @UiHandler("newSettings")
  void onKeyDown(KeyDownEvent event) {
    if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      addJdbcTableSettings();
      // Prevent the window from reloading
      event.preventDefault();
    }
  }

  public JsArray<JdbcValueTableSettingsFactoryDto> getJdbcTableSettingsFactories() {
    JsArray<JdbcValueTableSettingsFactoryDto> settingsArray = JsArrays.create();
    for (JdbcTableSettingsFactoryPanel panels : settingsList) settingsArray.push(panels.getJdbcTableSettingsFactory());
    return settingsArray;
  }

  public void addJdbcTableSettingsFactory(List<JdbcValueTableSettingsFactoryDto> settings) {
    if (settings == null) return;
    Collections.sort(settings, new Comparator<JdbcValueTableSettingsFactoryDto>() {
      @Override
      public int compare(JdbcValueTableSettingsFactoryDto o1, JdbcValueTableSettingsFactoryDto o2) {
        return fullName(o1).compareTo(fullName(o2));
      }

      private String fullName(JdbcValueTableSettingsFactoryDto dto)  {
        return dto.getSqlTable() + "(" + dto.getTablePartitionColumn() + ") - " + dto.getOpalTable();
      }

    });
    for (JdbcValueTableSettingsFactoryDto dto : settings) addJdbcTableSettingsFactory(dto);
  }

  public void addJdbcTableSettingsFactory(JdbcValueTableSettingsFactoryDto settings) {
    AccordionGroup group = new AccordionGroup();
    JdbcTableSettingsFactoryPanel settingsPanel = new JdbcTableSettingsFactoryPanel(settings, new JdbcTableSettingsFactoryHandler(group));
    addJdbcTableSettings(group, settingsPanel);
  }

  private void addJdbcTableSettings() {
    String sqlTableName = newSettings.getValue();
    if (Strings.isNullOrEmpty(sqlTableName) || Strings.isNullOrEmpty(sqlTableName.trim())) return;
    newSettings.setValue("", false);
    AccordionGroup group = new AccordionGroup();
    JdbcTableSettingsFactoryPanel settingsPanel = new JdbcTableSettingsFactoryPanel(sqlTableName, new JdbcTableSettingsFactoryHandler(group));
    addJdbcTableSettings(group, settingsPanel);
    group.toggle();
  }

  private void addJdbcTableSettings(AccordionGroup group, JdbcTableSettingsFactoryPanel settingsPanel) {
    group.add(settingsPanel);
    group.setHeading(settingsPanel.getLabel());
    accordion.add(group);
    settingsList.add(settingsPanel);
  }

  private class JdbcTableSettingsFactoryHandler implements JdbcTableSettingsFactoryPanel.GroupHandler {

    private final AccordionGroup group;

    public JdbcTableSettingsFactoryHandler(AccordionGroup group) {
      this.group = group;
    }

    @Override
    public void onRemove(JdbcTableSettingsFactoryPanel settings) {
      accordion.remove(group);
      settingsList.remove(settings);
    }

    @Override
    public void onLabelChange(JdbcTableSettingsFactoryPanel settings) {
      group.setHeading(settings.getLabel());
    }
  }
}
