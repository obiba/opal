/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import javax.annotation.Nullable;

import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.github.gwtbootstrap.client.ui.Divider;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Widget;

public abstract class CriterionDropdown extends DropdownButton {

  protected VariableDto variable;

  protected QueryResultDto queryResult;

  private final ListItem radioControls = new ListItem();

  public CriterionDropdown(VariableDto variableDto, @Nullable QueryResultDto termDto) {
    variable = variableDto;
    queryResult = termDto;

    updateCriterionFilter("All");

    radioControls.addStyleName("controls");

    int noEmpty = 0;
    if(queryResult != null) {

      // TODO: FacetArray for 1 variable always return only 1 facetArray ?
      if(queryResult.getFacetsArray().length() > 0) {
        if(queryResult.getFacetsArray().get(0).hasStatistics()) {
          // Statistics facet
          noEmpty += queryResult.getFacetsArray().get(0).getStatistics().getCount();
        } else {
          // Categories frequency facet
          for(int i = 0; i < queryResult.getFacetsArray().get(0).getFrequenciesArray().length(); i++) {
            noEmpty += queryResult.getFacetsArray().get(0).getFrequenciesArray().get(i).getCount();
          }
        }
      }
    }

    // All, Empty, Not Empty radio buttons
    RadioButton radioAll = getRadioButton("All", queryResult == null ? null : queryResult.getTotalHits());
    radioAll.setValue(true);
    radioControls.add(radioAll);
    radioControls.add(getRadioButton("Empty", queryResult == null ? null : queryResult.getTotalHits() - noEmpty));
    radioControls.add(getRadioButton("Not Empty", queryResult == null ? null : noEmpty));
    add(radioControls);

    Widget specificControls = getSpecificControls();
    if(specificControls != null) {
      add(new Divider());
      add(specificControls);
    }

    add(new Divider());

    NavLink remove = new NavLink("Remove");
    remove.setIcon(IconType.REMOVE);
    remove.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        removeFromParent();
      }
    });
    add(remove);

    // TODO:Remove clickHandler that closes the popup

  }

  private RadioButton getRadioButton(final String label, Integer count) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder().appendEscaped(label);

    if(count != null) {
      builder.appendHtmlConstant("<span style=\"font-size:x-small\"> (").append(count).appendEscaped(")")
          .appendHtmlConstant("</span>");
    }

    RadioButton radio = new RadioButton("radio", builder.toSafeHtml());

    radio.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        resetSpecificControls();
        updateCriterionFilter(label);
      }
    });

    return radio;
  }

  public void resetRadioControls() {
    for(int i = 0; i < radioControls.getWidgetCount(); i++) {
      ((RadioButton) radioControls.getWidget(i)).setValue(null);
    }
  }

  public void updateCriterionFilter(String filter) {
    setText(filter.isEmpty() ? variable.getName() : variable.getName() + ": " + filter);
  }

  public abstract Widget getSpecificControls();

  public abstract void resetSpecificControls();

  public abstract String getQueryString();

  @Override
  protected void onLoad() {
    super.onLoad();
    if(getTriggerWidget() != null) {
      bind(getTriggerWidget().getElement());
    }
  }

  // TODO: Find the selector that allows to skip the selection of the first input after the li of chosen options...
  private static native void bind(Element e) /*-{
    $wnd.jQuery(e).next().find('label, li').click(function(w) {
        w.stopPropagation();
    });

    $wnd.jQuery(e).next().find('input').not('input[autocomplete]').click(function(w) {
        w.stopPropagation();
    });
  }-*/;
}
