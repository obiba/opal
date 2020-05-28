/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Widget;

public abstract class IdentifiersCriterionDropdown extends ValueSetCriterionDropdown {

  private TextBox matches;


  public IdentifiersCriterionDropdown(RQLIdentifierCriterionParser criterion) {
    this(null, null);
    initialize(criterion);
  }

  public IdentifiersCriterionDropdown(String datasource, String table) {
    super(datasource, table,null, "identifier", null);
  }

  private void initialize(RQLIdentifierCriterionParser criterion) {
    if (criterion.isValid() && criterion.isLike()) {
      ((CheckBox) radioControls.getWidget(criterion.isNot() ? 2 : 1)).setValue(true);
      ((CheckBox) radioControls.getWidget(0)).setValue(false);
      matches.setText(criterion.getValueString());
      matches.setVisible(true);
      divider.setVisible(true);
    }
    else {
      ((CheckBox) radioControls.getWidget(0)).setValue(true);
      matches.setVisible(false);
      divider.setVisible(false);
    }
    setFilterText();
  }

  @Override
  public Widget createSpecificControls() {
    setupRadioControls();

    ListItem specificControls = new ListItem();
    matches = new TextBox();
    matches.setText("*");
    matches.addStyleName("bordered");
    specificControls.addStyleName("controls");
    matches.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        updateMatchCriteriaFilter();
      }
    });
    matches.setVisible(false);

    specificControls.add(matches);
    return specificControls;
  }

  @Override
  protected String getHeaderText() {
    return "ID";
  }

  @Override
  protected String getHeaderTitle() {
    return "";
  }

  @Override
  protected String getHeaderSubTitle() {
    return translations.criterionFiltersMap().get("entity_identifier");
  }

  private void setupRadioControls() {// Remove empty/not empty radio
    radioControls.remove(1);
    radioControls.remove(1);

    // Update radio controls
    RadioButton like = createRadioButton(translations.criterionFiltersMap().get("like"), null);
    like.addClickHandler(new OperatorClickHandler());
    radioControls.add(like);

    RadioButton not_like = createRadioButton(translations.criterionFiltersMap().get("not_like"), null);
    not_like.addClickHandler(new OperatorClickHandler());
    radioControls.add(not_like);
  }

  @Override
  public void resetSpecificControls() {
    matches.setVisible(false);
    if (divider != null) divider.setVisible(false);
    doFilter();
  }

  @Override
  public String getQueryString() {
    if(((CheckBox) radioControls.getWidget(0)).getValue()) {
      String emptyNotEmpty = super.getQueryString();
      if(emptyNotEmpty != null) return emptyNotEmpty;
    }

    String query = "(" + fieldName + ":(" + matches.getText() + ") OR " + fieldName + ".analyzed:(" + matches.getText() + "))";

    if(((CheckBox) radioControls.getWidget(1)).getValue() && !matches.getText().isEmpty()) {
      return query;
    }

    if(((CheckBox) radioControls.getWidget(2)).getValue() && !matches.getText().isEmpty()) {
      return "NOT " + query;
    }

    return null;
  }

  @Override
  public String getRQLQueryString() {
    if (getRadioButtonValue(0)) {
      String emptyNotEmpty = super.getRQLQueryString();
      if (emptyNotEmpty != null) return emptyNotEmpty;
    }
    String query = "like(" + getRQLField() + ",(" + matches.getText() + "))";
    if (getRadioButtonValue(1)) return query;
    if (getRadioButtonValue(2)) return "not(" + query + ")";
    return null;
  }

  @Override
  protected String getMagmaJsStatement() {
    // all
    if (getRadioButtonValue(0)) return "";
    String statement = "$id()";
    String match = ".matches(/" + matches.getText()  + "/)";
    if (getRadioButtonValue(1)) {
      if (matches.getText().equals("*")) return "";
      if (Strings.isNullOrEmpty(matches.getText())) return statement + ".isNull()";
      return statement + match;
    }
    if (getRadioButtonValue(2)) {
      if (matches.getText().equals("*")) return statement + ".isNull()";
      if (Strings.isNullOrEmpty(matches.getText())) return "";
      return statement + match + ".not()";
    }
    return "";
  }

  @Override
  protected String getRQLField() {
    return fieldName;
  }

  protected void updateCriterionFilter(String filter) {
    setText(filter.isEmpty() ? "ID" : "ID " + filter.toLowerCase());
  }

  private void updateMatchCriteriaFilter() {
    setFilterText();
    doFilter();
  }

  private void setFilterText() {
    if(getRadioButtonValue(1)) {
      String prefix = translations.criterionFiltersMap().get("like").toLowerCase() + " (";
      updateCriterionFilter(prefix + matches.getText() + ")");
    } else if (getRadioButtonValue(2)) {
      String prefix = translations.criterionFiltersMap().get("not_like").toLowerCase() + " (";
      updateCriterionFilter(prefix + matches.getText() + ")");
    }
    else
      updateCriterionFilter(translations.criterionFiltersMap().get("all").toLowerCase());
  }

  private class OperatorClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      matches.setVisible(true);
      divider.setVisible(true);
      setFilterText();
      doFilter();
    }
  }
}
