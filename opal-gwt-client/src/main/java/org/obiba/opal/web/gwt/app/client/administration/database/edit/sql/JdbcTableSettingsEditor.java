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
import org.obiba.opal.web.model.client.magma.JdbcValueTableSettingsDto;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JdbcTableSettingsEditor extends Composite {

  interface JdbcTableSettingsEditorUiBinder extends UiBinder<Widget, JdbcTableSettingsEditor> {}

  private static final JdbcTableSettingsEditorUiBinder uiBinder = GWT.create(JdbcTableSettingsEditorUiBinder.class);

  @UiField
  TextBox newSettings;

  @UiField
  Accordion accordion;

  private List<JdbcTableSettingsPanel> settingsList;

  public JdbcTableSettingsEditor() {
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

  public JsArray<JdbcValueTableSettingsDto> getJdbcTableSettings() {
    JsArray<JdbcValueTableSettingsDto> settingsArray = JsArrays.create();
    for (JdbcTableSettingsPanel panels : settingsList) settingsArray.push(panels.getJdbcTableSettings());
    return settingsArray;
  }

  public void addJdbcTableSettings(List<JdbcValueTableSettingsDto> settings) {
    if (settings == null) return;
    Collections.sort(settings, new Comparator<JdbcValueTableSettingsDto>() {
      @Override
      public int compare(JdbcValueTableSettingsDto o1, JdbcValueTableSettingsDto o2) {
        return fullName(o1).compareTo(fullName(o2));
      }
      private String fullName(JdbcValueTableSettingsDto dto)  {
        return dto.getSqlTable() + " - " + dto.getOpalTable();
      }
    });
    for (JdbcValueTableSettingsDto dto : settings) addJdbcTableSettings(dto);
  }

  public void addJdbcTableSettings(JdbcValueTableSettingsDto settings) {
    AccordionGroup group = new AccordionGroup();
    JdbcTableSettingsPanel settingsPanel = new JdbcTableSettingsPanel(settings, new JdbcTableSettingsHandler(group));
    addJdbcTableSettings(group, settingsPanel);
  }

  private void addJdbcTableSettings() {
    String sqlTableName = newSettings.getValue();
    if (Strings.isNullOrEmpty(sqlTableName) || Strings.isNullOrEmpty(sqlTableName.trim())) return;
    newSettings.setValue("", false);
    AccordionGroup group = new AccordionGroup();
    JdbcTableSettingsPanel settingsPanel = new JdbcTableSettingsPanel(sqlTableName, new JdbcTableSettingsHandler(group));
    addJdbcTableSettings(group, settingsPanel);
    group.toggle();
  }

  private void addJdbcTableSettings(AccordionGroup group, JdbcTableSettingsPanel settingsPanel) {
    group.add(settingsPanel);
    group.setHeading(settingsPanel.getLabel());
    accordion.add(group);
    settingsList.add(settingsPanel);
  }

  private class JdbcTableSettingsHandler implements JdbcTableSettingsPanel.GroupHandler {

    private final AccordionGroup group;

    public JdbcTableSettingsHandler(AccordionGroup group) {
      this.group = group;
    }

    @Override
    public void onRemove(JdbcTableSettingsPanel settings) {
      accordion.remove(group);
      settingsList.remove(settings);
    }

    @Override
    public void onLabelChange(JdbcTableSettingsPanel settings) {
      group.setHeading(settings.getLabel());
    }
  }
}
