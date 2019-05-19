/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importvariables.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.view.AttributesTable;
import org.obiba.opal.web.gwt.app.client.magma.view.CategoriesTable;
import org.obiba.opal.web.gwt.app.client.ui.NavTabsPanel;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 *
 */
public class TableCompareVariablePanel extends FlowPanel {

  private static final Translations translations = GWT.create(Translations.class);

  private final VariableDto variableDto;

  public TableCompareVariablePanel(VariableDto variableDto) {
    this.variableDto = variableDto;

    add(initProperties());
    add(initTabs());
  }

  private PropertiesTable initProperties() {
    PropertiesTable properties = new PropertiesTable();
    properties.setKeyStyleNames("span2");
    properties.setValueStyleNames("span8");

    properties.addProperty(translations.entityTypeLabel(), variableDto.getEntityType());
    properties.addProperty(translations.referencedEntityTypeLabel(), variableDto.getReferencedEntityType(), 1);
    properties.addProperty(translations.valueTypeLabel(), variableDto.getValueType());
    properties.addProperty(translations.mimeTypeLabel(), variableDto.getMimeType(), 1);
    properties.addProperty(translations.repeatableLabel(),
        variableDto.getIsRepeatable() ? translations.yesLabel() : translations.noLabel());
    properties.addProperty(translations.occurrenceGroupLabel(), variableDto.getOccurrenceGroup(), 1);
    properties.addProperty(translations.unitLabel(), variableDto.getUnit(), 0, 2);

    return properties;
  }

  private NavTabsPanel initTabs() {
    NavTabsPanel tabs = new NavTabsPanel();
    tabs.addStyleName("top-margin");
    tabs.add(initTablePanel(new CategoriesTable(variableDto)), translations.categoriesLabel());
    tabs.add(initTablePanel(new AttributesTable(variableDto)), translations.attributesLabel());
    return tabs;
  }

  private Panel initTablePanel(Table<?> table) {
    FlowPanel panel = new FlowPanel();
    SimplePager pager = new SimplePager();
    pager.setDisplay(table);
    pager.addStyleName("pull-right");
    panel.add(pager);
    table.addStyleName("pull-left");
    table.setWidth("100%");
    panel.add(table);
    return panel;
  }

}
