/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;

public abstract class CriterionDropdown extends DropdownButton {

  protected final Translations translations = GWT.create(Translations.class);

  protected String fieldName;

  protected final ListItem radioControls = new ListItem();

  protected final String groupId;

  public CriterionDropdown(String fieldName) {
    this.fieldName = extractField(fieldName).replace(' ', '+');
    groupId = String.valueOf(Random.nextInt(1000000)); //to be used in radio button names, to make sure they don't clash
    setSize(ButtonSize.SMALL);
    radioControls.addStyleName("controls");

    addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        onDropdownChange();
      }
    });
  }

  protected abstract void onDropdownChange();

  protected abstract Widget createSpecificControls();

  protected abstract void resetSpecificControls();

  public abstract void doFilter();

  public String getQueryString() {
    if(getRadioButtonValue(1)) {
      // Empty
      return "NOT _exists_:" + fieldName;
    }
    if(getRadioButtonValue(2)) {
      // Not empty
      return "_exists_:" + fieldName;
    }
    if(getRadioButtonValue(0)) {
      // All: No filter is necessary
      return "";
    }

    return null;
  }

  public String getRQLQueryString() {
    if(getRadioButtonValue(1)) {
      // Empty
      return "not(exists(" + getRQLField() + "))";
    }
    if(getRadioButtonValue(2)) {
      // Not empty
      return "exists(" + getRQLField() + ")";
    }
    if(getRadioButtonValue(0)) {
      // All: No filter is necessary
      return "all(" + getRQLField() + ")";
    }

    return null;
  }

  public boolean isAll() {
    return getRadioButtonValue(0);
  }

  protected String getRQLField() {
    return fieldName;
  }

  protected boolean getRadioButtonValue(int idx) {
    return ((CheckBox) radioControls.getWidget(idx)).getValue();
  }

  protected RadioButton createRadioButton(final String label, Integer count) {
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

  protected RadioButton createRadioButtonResetSpecific(String label, Integer count) {
    RadioButton radio = createRadioButton(label, count);
    radio.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        resetSpecificControls();
      }
    });
    return radio;
  }

  protected void updateCriterionFilter(String filter) {
    setText(filter.isEmpty() ? fieldName : fieldName + ": " + filter);
  }

  protected String extractField(String fieldQuery) {
    int idx = fieldQuery.indexOf(':');
    return idx>0 ? fieldQuery.substring(0, idx) : fieldQuery;
  }

  protected String extractValue(String fieldQuery) {
    int idx = fieldQuery.indexOf(':');
    return idx>0 ? fieldQuery.substring(idx + 1) : "";
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