/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Table;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;

import static org.obiba.opal.web.model.client.opal.TaxonomyDto.TermDto;

/**
 *
 */
public class TermsTable extends Table<TermDto> {

  private static final Translations translations = GWT.create(Translations.class);

  public TermsTable() {
    this(null);
  }

  public TermsTable(TermDto termDto) {
    initColumns();

    if(termDto != null) {
      JsArrayDataProvider<TermDto> provider = new JsArrayDataProvider<TermDto>();
      provider.setArray(JsArrays.toSafeArray(termDto.getTermsArray()));
      provider.addDataDisplay(this);
    }
  }

  private void initColumns() {
    setPageSize(Table.DEFAULT_PAGESIZE);
    setEmptyTableWidget(new InlineLabel(translations.noCategoriesLabel()));

    Column<TermDto, String> name = new Column<TermDto, String>(new EditTextCell()) {
      @Override
      public String getValue(TermDto object) {
        return object.getName();
      }
    };
    addColumn(name);

//    addColumn(new EditableColumn<TermDto>(new TextInputCell()) {
//      @Override
//      public String getValue(TermDto object) {
//        return object.getName();
//      }
//    }, translations.nameLabel());

// addColumn(new EditableColumn<TaxonomyDto.TermDto>() {
//      @Override
//      public String getValue(TaxonomyDto.TermDto object) {
//        return object.getName();
//      }
//    }, translations.nameLabel());

//    addColumn(new CategoryAttributeColumn("label"), translations.labelLabel());

    addColumn(new TextColumn<TermDto>() {
      @Override
      public String getValue(TermDto object) {
        return "";
      }
    });
  }
}
