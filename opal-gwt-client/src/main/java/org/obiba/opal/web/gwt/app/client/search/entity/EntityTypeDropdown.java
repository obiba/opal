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

import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.obiba.opal.web.model.client.magma.VariableEntitySummaryDto;

import java.util.List;

public class EntityTypeDropdown extends DropdownButton {

  private String selection;

  public void setEntityTypes(List<VariableEntitySummaryDto> entityTypes, String selectedType) {
    clear();
    String selectedEntityType = Strings.isNullOrEmpty(selectedType) ? "Participant" : selectedType;
    boolean hasSelectedEntityType = false;
    boolean hasParticipantType = false;
    for (VariableEntitySummaryDto typeSummary : entityTypes) {
      final NavLink item = new NavLink(typeSummary.getEntityType());
      item.setName(typeSummary.getEntityType());
      item.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          setSelection(item.getName());
        }
      });
      add(item);
      if (selectedEntityType.equals(typeSummary.getEntityType())) hasSelectedEntityType = true;
      if ("Participant".equals(typeSummary.getEntityType())) hasParticipantType = true;
    }
    if (hasSelectedEntityType) setSelection(selectedEntityType);
    else if (!entityTypes.isEmpty()) {
      if (hasParticipantType) setSelection("Participant");
      else setSelection(entityTypes.get(0).getEntityType());
    }
  }

  public String getSelection() {
    return selection;
  }

  public void setSelection(String selectedType) {
    this.selection = Strings.isNullOrEmpty(selectedType) ? "Participant" : selectedType.trim();
    super.setText(selection);
  }

  @Override
  public void setText(String text) {
    setSelection(text);
  }
}
