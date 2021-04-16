/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.variables;

import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.magma.VariableEntitySummaryDto;

import java.util.List;

public class SortDropdown extends DropdownButton {

  private static final Translations translations = GWT.create(Translations.class);

  private String selection;

  private List<ClickHandler> listeners = Lists.newArrayList();

  public SortDropdown() {
    List<String> sortsWithOrder = Lists.newArrayList();
    sortsWithOrder.add("_score:desc");
    sortsWithOrder.add("name:asc");
    sortsWithOrder.add("name:desc");
    initialize(sortsWithOrder);
  }

  public void initialize(List<String> sortsWithOrder) {
    clear();
    String first = null;
    for (String sortWithOrder : sortsWithOrder) {
      final NavLink item = new NavLink(translate(sortWithOrder));
      item.setName(sortWithOrder);
      item.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          setSelection(item.getName());
          for (ClickHandler handler : listeners)
            handler.onClick(event);
        }
      });
      add(item);
      if (first == null) first = sortWithOrder;
    }
    setSelection(first);
  }

  public void addClickListener(ClickHandler handler) {
    listeners.add(handler);
  }

  public String getSelection() {
    return selection;
  }

  public void setSelection(String selectedSort) {
    this.selection = selectedSort.trim();
    super.setText(translate(selection));
  }

  @Override
  public void setText(String text) {
    setSelection(text);
  }

  private String translate(String sortWithOrder) {
    String[] tokens = sortWithOrder.split(":");
    if (translations.sortOrderMap().containsKey(sortWithOrder))
        return translations.sortOrderMap().get(sortWithOrder);
    return tokens[0] + " (" + tokens[1] + ")";
  }
}
