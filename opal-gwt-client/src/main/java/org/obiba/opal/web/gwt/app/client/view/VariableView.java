/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.VariablePresenter;
import org.obiba.opal.web.model.client.AttributeDto;
import org.obiba.opal.web.model.client.CategoryDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.ListView.Delegate;

/**
 *
 */
public class VariableView extends Composite implements VariablePresenter.Display {

  @UiTemplate("VariableView.ui.xml")
  interface VariableViewUiBinder extends UiBinder<Widget, VariableView> {
  }

  private static VariableViewUiBinder uiBinder = GWT.create(VariableViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  Label variableName;

  @UiField
  Label entityType;

  @UiField
  Label valueType;

  @UiField
  Label mimeType;

  @UiField
  Label unit;

  @UiField
  Label repeatable;

  @UiField
  Label occurrenceGroup;

  @UiField
  Label categoryTableTitle;

  @UiField
  CellTable<CategoryDto> categoryTable;

  @UiField
  Label attributeTableTitle;

  @UiField
  CellTable<AttributeDto> attributeTable;

  //
  // Constructors
  //

  public VariableView() {
    initWidget(uiBinder.createAndBindUi(this));

    initCategoryTable();
    initAttributeTable();
  }

  //
  // VariablePresenter.Display Methods
  //

  @SuppressWarnings("unchecked")
  public void renderCategoryRows(JsArray<CategoryDto> rows) {
    final JsArray<CategoryDto> categoryRows = (rows != null) ? rows : (JsArray<CategoryDto>) JsArray.createArray();

    categoryTableTitle.setText(translations.categoriesLabel() + " (" + categoryRows.length() + ")");

    categoryTable.setDelegate(new Delegate<CategoryDto>() {

      @Override
      public void onRangeChanged(ListView<CategoryDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(categoryRows, start, length));
      }
    });

    categoryTable.setData(0, categoryRows.length(), JsArrays.toList(categoryRows, 0, categoryRows.length()));
    categoryTable.setDataSize(categoryRows.length(), true);
  }

  @SuppressWarnings("unchecked")
  public void renderAttributeRows(final JsArray<AttributeDto> rows) {
    final JsArray<AttributeDto> attributeRows = (rows != null) ? rows : (JsArray<AttributeDto>) JsArray.createArray();

    attributeTableTitle.setText(translations.attributesLabel() + " (" + attributeRows.length() + ")");

    attributeTable.setDelegate(new Delegate<AttributeDto>() {

      @Override
      public void onRangeChanged(ListView<AttributeDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(attributeRows, start, length));
      }
    });

    attributeTable.setData(0, attributeRows.length(), JsArrays.toList(attributeRows, 0, attributeRows.length()));
    attributeTable.setDataSize(attributeRows.length(), true);
  }

  public Widget asWidget() {
    return this;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  //
  // Methods
  //

  public HasText getVariableNameLabel() {
    return variableName;
  }

  public HasText getEntityTypeLabel() {
    return entityType;
  }

  public HasText getValueTypeLabel() {
    return valueType;
  }

  public HasText getMimeTypeLabel() {
    return mimeType;
  }

  public HasText getUnitLabel() {
    return unit;
  }

  public HasText getRepeatableLabel() {
    return repeatable;
  }

  public HasText getOccurrenceGroupLabel() {
    return occurrenceGroup;
  }

  private void initCategoryTable() {
    categoryTableTitle.setText(translations.categoriesLabel());
    categoryTable.setSelectionEnabled(false);
    categoryTable.setSelectionModel(new SingleSelectionModel<CategoryDto>());

    addCategoryTableColumns();
  }

  private void addCategoryTableColumns() {
    categoryTable.addColumn(new TextColumn<CategoryDto>() {
      @Override
      public String getValue(CategoryDto object) {
        return object.getName();
      }
    }, translations.nameLabel());

    categoryTable.addColumn(new TextColumn<CategoryDto>() {
      @Override
      public String getValue(CategoryDto object) {
        return Boolean.toString(object.getIsMissing());
      }
    }, translations.missingLabel());
  }

  private void initAttributeTable() {
    attributeTableTitle.setText(translations.attributesLabel());
    attributeTable.setSelectionEnabled(false);
    attributeTable.setSelectionModel(new SingleSelectionModel<AttributeDto>());

    addAttributeTableColumns();
  }

  private void addAttributeTableColumns() {
    attributeTable.addColumn(new TextColumn<AttributeDto>() {
      @Override
      public String getValue(AttributeDto object) {
        return object.getName();
      }
    }, translations.nameLabel());

    attributeTable.addColumn(new TextColumn<AttributeDto>() {
      @Override
      public String getValue(AttributeDto object) {
        return object.getLocale();
      }
    }, translations.languageLabel());

    attributeTable.addColumn(new TextColumn<AttributeDto>() {
      @Override
      public String getValue(AttributeDto object) {
        return object.getValue();
      }
    }, "Value"); // translations.valueLabel());
  }
}
