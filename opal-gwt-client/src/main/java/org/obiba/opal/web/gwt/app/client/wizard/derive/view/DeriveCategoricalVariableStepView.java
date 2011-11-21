/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveCategoricalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DeriveCategoricalVariableStepView extends Composite implements DeriveCategoricalVariableStepPresenter.Display {

  @UiTemplate("DeriveCategoricalVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveCategoricalVariableStepView> {
  }

  interface Template extends SafeHtmlTemplates {

    @com.google.gwt.safehtml.client.SafeHtmlTemplates.Template("<span class=\"{0}\" title=\"{1}\">{2}</span>")
    SafeHtml spanWithTile(String cssClass, String title, SafeHtml cellContent);

  }

  private static Template template = GWT.create(Template.class);

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private Translations translations = GWT.create(Translations.class);

  @UiField
  WizardStep mapStep;

  @UiField(provided = true)
  ValueMapGrid valuesMapGrid;

  //
  // Constructors
  //

  public DeriveCategoricalVariableStepView() {
    valuesMapGrid = new ValueMapGrid() {
      @Override
      protected void initializeValueColumn() {
        ValueMapCell cell = new ValueMapCell() {

          @Override
          protected String getText(ValueMapEntry entry) {
            return entry.getValue();
          }

          @Override
          public void render(com.google.gwt.cell.client.Cell.Context context, ValueMapEntry entry, SafeHtmlBuilder sb) {
            if(entry != null) {
              String cssClasses = getCssClasses(entry.getType());
              SafeHtml safeHtml = SafeHtmlUtils.fromString(getText(entry));
              if(entry.getType() == ValueMapEntryType.DISTINCT_VALUE) {
                sb.append(DeriveCategoricalVariableStepView.template.spanWithTile(cssClasses + " warning", translations.valueIsNotACategory(), safeHtml));
              } else {
                sb.append(template.span(cssClasses, safeHtml));
              }
            }
          }
        };

        // Value
        Column<ValueMapEntry, ValueMapEntry> valueColumn = new Column<ValueMapEntry, ValueMapEntry>(cell) {

          @Override
          public ValueMapEntry getValue(ValueMapEntry entry) {
            return entry;
          }

        };
        valueColumn.setCellStyleNames("original-value");
        table.addColumn(valueColumn, translations.originalValueLabel());
      }
    };
    valuesMapGrid.setWidth("100%");
    valuesMapGrid.setPageSizeMin(13);
    valuesMapGrid.setPageSizeMax(100);
    valuesMapGrid.setGridHeight("40em");

    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public DefaultWizardStepController.Builder getMapStepController() {
    return DefaultWizardStepController.Builder.create(mapStep).title(translations.recodeCategoriesStepTitle());
  }

  @Override
  public void populateValues(List<ValueMapEntry> valuesMap) {
    valuesMapGrid.populate(valuesMap);
  }

  //
  // Widget Display methods
  //

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void setMaxFrequency(double maxFrequency) {
    valuesMapGrid.setMaxFrequency(maxFrequency);
  }

  @Override
  public void enableFrequencyColumn(boolean enableFrequencyColumn) {
    valuesMapGrid.enableFrequencyColumn(enableFrequencyColumn);
  }

}
