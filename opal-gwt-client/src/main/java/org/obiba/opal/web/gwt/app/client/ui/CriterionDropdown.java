/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gwt.user.client.Random;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Widget;

public abstract class CriterionDropdown extends DropdownButton {

  protected final Translations translations = GWT.create(Translations.class);

  protected VariableDto variable;

  protected QueryResultDto queryResult;

  protected String fieldName;

  protected final ListItem radioControls = new ListItem();

  private final String groupId;

  CriterionDropdown(VariableDto variableDto, @Nonnull String fieldName, @Nullable QueryResultDto termDto) {
    variable = variableDto;
    this.fieldName = fieldName.replace(' ', '+');
    queryResult = termDto;

    groupId = String.valueOf(Random.nextInt(1000000)); //to be used in radio button names, to make sure they don't clash

    setSize(ButtonSize.SMALL);
    updateCriterionFilter(translations.criterionFiltersMap().get("all"));

    radioControls.addStyleName("controls");

    addRadioButtons(getNoEmptyCount());

    Widget specificControls = getSpecificControls();
    if(specificControls != null) {
      add(specificControls);
    }
  }

  private int getNoEmptyCount() {
    int nb = 0;
    if(queryResult != null) {

      if(queryResult.getFacetsArray().length() > 0) {
        if(queryResult.getFacetsArray().get(0).hasStatistics()) {
          // Statistics facet
          nb += queryResult.getFacetsArray().get(0).getStatistics().getCount();
        } else {
          // Categories frequency facet
          for(int i = 0; i < queryResult.getFacetsArray().get(0).getFrequenciesArray().length(); i++) {
            nb += queryResult.getFacetsArray().get(0).getFrequenciesArray().get(i).getCount();
          }
        }
      }
    }
    return nb;
  }

  private void addRadioButtons(int noEmpty) {
    // All, Empty, Not Empty radio buttons
    RadioButton radioAll = getRadioButtonResetSpecific(translations.criterionFiltersMap().get("all"),
        queryResult == null ? null : queryResult.getTotalHits());
    radioAll.setValue(true);
    radioControls.add(radioAll);

    radioControls.add(getRadioButtonResetSpecific(translations.criterionFiltersMap().get("empty"),
        queryResult == null ? null : queryResult.getTotalHits() - noEmpty));
    radioControls.add(getRadioButtonResetSpecific(translations.criterionFiltersMap().get("not_empty"),
        queryResult == null ? null : noEmpty));
    add(radioControls);
  }

  private RadioButton getRadioButtonResetSpecific(String label, Integer count) {
    RadioButton radio = getRadioButton(label, count);
    radio.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        resetSpecificControls();
      }
    });
    return radio;
  }

  protected RadioButton getRadioButton(final String label, Integer count) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder().appendEscaped(label);

    if(count != null) {
      builder.appendHtmlConstant("<span style=\"font-size:x-small\"> (").append(count).appendEscaped(")")
          .appendHtmlConstant("</span>");
    }

      RadioButton radio = new RadioButton(fieldName + "-radio-" + this.groupId, builder.toSafeHtml());

    radio.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        updateCriterionFilter(label);
      }
    });

    return radio;
  }

  protected void updateCriterionFilter(String filter) {
    setText(filter.isEmpty() ? variable.getName() : variable.getName() + ": " + filter);
  }

  protected abstract Widget getSpecificControls();

  protected abstract void resetSpecificControls();

  public abstract void doFilterValueSets();

  public String getQueryString() {
    if(((CheckBox) radioControls.getWidget(0)).getValue()) {
      // All: No filter is necessary
      return "";
    }
    if(((CheckBox) radioControls.getWidget(1)).getValue()) {
      // Not empty
      return "_missing_:" + fieldName;
    }
    if(((CheckBox) radioControls.getWidget(2)).getValue()) {
      // Empty
      return "_exists_:" + fieldName;
    }

    return null;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if(getTriggerWidget() != null) {
      bind(getTriggerWidget().getElement());
    }
  }

  // TODO: Find the selector that allows to skip the selection of the first input after the li of chosen options...
  private static native void bind(Element e) /*-{
    $wnd.jQuery(e).next().find('label, li').click(function (w) {
      w.stopPropagation();
    });

    $wnd.jQuery(e).next().find('input').not('input[autocomplete]').click(function (w) {
      w.stopPropagation();
    });
  }-*/;
}