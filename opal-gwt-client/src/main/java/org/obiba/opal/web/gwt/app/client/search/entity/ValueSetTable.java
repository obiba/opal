/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.entity;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueRenderer;

public class ValueSetTable extends Table<VariableValueRow> {

  private VariableValueRowClickableColumn valueColumn;

  public ValueSetTable() {
    initColumns(null);
  }

  public void setVariableValueSelectionHandler(final VariableValueSelectionHandler handler) {
    valueColumn.setFieldUpdater(
        new FieldUpdater<VariableValueRow, VariableValueRow>() {
          @Override
          public void update(int index, VariableValueRow variableValueRow, VariableValueRow value) {
            handler.onValueSelection(variableValueRow);
          }
        });
  }

  private void initColumns(PlaceManager placeManager) {
    setPageSize(Table.DEFAULT_PAGESIZE);
    setEmptyTableWidget(new InlineLabel(translations.noValuesLabel()));

    if (placeManager != null) {
      addColumn(new VariableValueRowColumn(new ProjectTableLinkCell(placeManager)), translations.tableLabel());
      addColumn(new VariableValueRowColumn(new VariableLinkCell(placeManager)), translations.variableLabel());
    } else {
      addColumn(new TextColumn<VariableValueRow>() {
        @Override
        public String getValue(VariableValueRow row) {
          return row.getVariable();
        }
      }, translations.variableLabel());
    }

    // Variable value column having each cell of different type (text, binary, repeatable)
    valueColumn = new VariableValueRowClickableColumn();
    addColumn(valueColumn, translations.valueLabel());
  }

  public void setPlaceManager(PlaceManager placeManager) {
    while (getColumnCount()>0) {
      removeColumn(0);
    }
    initColumns(placeManager);
  }

  public interface VariableValueSelectionHandler {
    void onValueSelection(VariableValueRow variableValueRow);
  }

  private static class VariableValueRowClickableColumn extends Column<VariableValueRow, VariableValueRow> {

    private VariableValueRowClickableColumn() {
      super(new ClickableValueCell());
    }

    @Override
    public VariableValueRow getValue(VariableValueRow object) {
      return object;
    }

  }

  private static class ProjectTableLinkCell extends PlaceRequestCell<VariableValueRow> {

    private ProjectTableLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(VariableValueRow row) {
      return ProjectPlacesHelper.getTablePlace(row.getDatasource(), row.getTable());
    }

    @Override
    public String getText(VariableValueRow row) {
      return row.getDatasource() + "." + row.getTable();
    }

    @Override
    public String getIcon(VariableValueRow value) {
      return "icon-table";
    }
  }


  private static class VariableLinkCell extends PlaceRequestCell<VariableValueRow> {

    private VariableLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(VariableValueRow row) {
      return ProjectPlacesHelper.getVariablePlace(row.getDatasource(), row.getTable(), row.getVariable());
    }

    @Override
    public String getText(VariableValueRow row) {
      return row.getVariable();
    }
  }

  /**
   * Specialized class to render each cell depending on the variable type (text, repeatable, binary)
   */
  private static class ClickableValueCell extends AbstractSafeHtmlCell<VariableValueRow> {

    private ClickableValueCell() {
      this(new AbstractSafeHtmlRenderer<VariableValueRow>() {
        @Override
        public SafeHtml render(VariableValueRow object) {
          String valueStr = renderValue(object);
          if (valueStr == null)
            return renderNullValue();
          if(valueStr.trim().isEmpty())
            return new SafeHtmlBuilder().toSafeHtml();
          if(object.getVariableDto().getIsRepeatable())
            return renderLink(valueStr, IconType.LIST);
          if(object.getVariableDto().getValueType().equalsIgnoreCase("binary"))
            return renderLink(valueStr, IconType.DOWNLOAD);
          if(object.getVariableDto().getValueType().matches("point|linestring|polygon"))
            return renderLink(valueStr, IconType.MAP_MARKER);
          if(object.getVariableDto().getValueType().equalsIgnoreCase("text") &&
              !Strings.isNullOrEmpty(object.getVariableDto().getReferencedEntityType()))
            return renderLink(valueStr, IconType.ELLIPSIS_VERTICAL);
          return SimpleSafeHtmlRenderer.getInstance().render(valueStr);
        }

        private SafeHtml renderNullValue() {
          return new SafeHtmlBuilder()
              .appendHtmlConstant("<span class='help-block no-bottom-margin' style='font-size: smaller'>(null)</span>")
              .toSafeHtml();
        }

        private SafeHtml renderLink(String valueStr, IconType iconType) {
          Icon i = new Icon(iconType);
          i.setIconSize(IconSize.LARGE);
          i.addStyleName("xsmall-right-indent");
          return new SafeHtmlBuilder().appendHtmlConstant("<a class=\"iconb\">").appendHtmlConstant(i.toString())
              .appendEscaped(valueStr).appendHtmlConstant("</a>").toSafeHtml();
        }

        private String renderValue(VariableValueRow row) {
          ValueRenderer valueRender = ValueRenderer.valueOf(row.getVariableDto().getValueType().toUpperCase());
          return valueRender.render(row.getValueDto(), row.getVariableDto().getIsRepeatable());
        }
      });
    }

    private ClickableValueCell(SafeHtmlRenderer<VariableValueRow> renderer) {
      super(renderer, "click", "keydown");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, VariableValueRow value,
                               NativeEvent event, ValueUpdater<VariableValueRow> valueUpdater) {
      super.onBrowserEvent(context, parent, value, event, valueUpdater);
      if("click".equals(event.getType())) {
        onEnterKeyDown(context, parent, value, event, valueUpdater);
      }
    }

    @Override
    protected void onEnterKeyDown(Context context, Element parent, VariableValueRow value,
                                  NativeEvent event, ValueUpdater<VariableValueRow> valueUpdater) {
      if(valueUpdater != null) {
        valueUpdater.update(value);
      }
    }

    @Override
    protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
      if(value != null) {
        sb.append(value);
      }
    }
  }

  private static class VariableValueRowColumn extends Column<VariableValueRow, VariableValueRow> {
    public VariableValueRowColumn(Cell<VariableValueRow> cell) {
      super(cell);
    }

    @Override
    public VariableValueRow getValue(VariableValueRow row) {
      return row;
    }
  }
}
