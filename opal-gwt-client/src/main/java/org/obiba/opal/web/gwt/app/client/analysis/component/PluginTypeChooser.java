package org.obiba.opal.web.gwt.app.client.analysis.component;

import com.google.gwt.uibinder.client.UiConstructor;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.model.client.opal.AnalysisPluginTemplateDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginTypeChooser extends Chooser {

  private Map<String, GroupItem> groupItemMap = new HashMap<String, GroupItem>();
  private String currentGroup;
  private int itemCount = 0;

  @UiConstructor
  public PluginTypeChooser(boolean isMultipleSelect) {
    super(isMultipleSelect);
    initWidget();
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

  public SelectedData getSelectedData() {
    int index = super.getSelectedIndex();
    for (GroupItem groupItem : groupItemMap.values()) {
      if (groupItem.indices.contains(index)) {
        return new SelectedData(groupItem.plugin, groupItem.templates.get(index));
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
   * Set the selected value under the corresponding group
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

  public void setSelectedTemplateIndex(int templateIndex) {
    for (GroupItem groupItem : groupItemMap.values()) {
      if (groupItem.indices.contains(templateIndex)) {
        setSelectedIndex(templateIndex);
//        setSelectedValue(groupItem.templates.get(templateIndex).getTitle());
      }
    }
  }

  private void initWidget() {
    setPlaceholderText("Put something...");
  }

  public class SelectedData {
    private final PluginPackageDto plugin;
    private final AnalysisPluginTemplateDto template;

    public SelectedData(PluginPackageDto plugin, AnalysisPluginTemplateDto template) {
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
