/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.analysis.component;

import com.google.gwt.uibinder.client.UiConstructor;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.model.client.opal.AnalysisPluginTemplateDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginTemplateChooser extends Chooser {

  private Map<String, GroupItem> groupItemMap = new HashMap<String, GroupItem>();
  private String currentGroup;
  private int itemCount = 0;

  @UiConstructor
  public PluginTemplateChooser(boolean isMultipleSelect) {
    super(isMultipleSelect);
  }

  public void addPluginAndTemplates(PluginPackageDto plugin, List<AnalysisPluginTemplateDto> templates) {
    currentGroup = plugin.getName();

    GroupItem groupItem = new GroupItem(plugin, templates);
    groupItemMap.put(currentGroup, groupItem);
    String group = plugin.getTitle();
    addGroup(group);

    for (AnalysisPluginTemplateDto template : templates) {
      groupItem.indices.add(itemCount++);
      addItemToGroup(template.getTitle());
    }
  }

  public SelectionData getSelectedData() {
    int index = super.getSelectedIndex();
    for (GroupItem groupItem : groupItemMap.values()) {
      if (groupItem.indices.contains(index)) {
        return new SelectionData(groupItem.plugin, groupItem.templates.get(index));
      }
    }

    return null;
  }

  @Override
  public void clear() {
    groupItemMap.clear();
    super.clear();
  }

  /**
   * Set the selected value under the corresponding plugin group
   *
   * @param pluginName
   * @param templateName
   */
  public void setSelectedTemplate(String pluginName, String templateName) {
    GroupItem groupItem = groupItemMap.get(pluginName);
    if (groupItem != null) {
      super.setSelectedIndex(groupItem.findTemplateIndex(templateName));
    }
  }


  public class SelectionData {
    private final PluginPackageDto plugin;
    private final AnalysisPluginTemplateDto template;

    public SelectionData(PluginPackageDto plugin, AnalysisPluginTemplateDto template) {
      this.plugin = plugin;
      this.template = template;
    }

    public PluginPackageDto getPlugin() {
      return plugin;
    }

    public AnalysisPluginTemplateDto getTemplate() {
      return template;
    }
  }

  private static class GroupItem {
    private final PluginPackageDto plugin;

    private List<Integer> indices = new ArrayList<Integer>();

    private List<AnalysisPluginTemplateDto> templates = new ArrayList<AnalysisPluginTemplateDto>();

    GroupItem(PluginPackageDto dto, List<AnalysisPluginTemplateDto> templateDtos) {
      plugin = dto;
      templates = templateDtos;
    }

    int findTemplateIndex(String templateName) {
      for (int i = 0; i < templates.size(); i++) {
        if (templates.get(i).getName().equals(templateName)) {
          return i;
        }
      }

      return -1;
    }
  }

}
