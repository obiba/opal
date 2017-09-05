/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.Label;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;

/**
 * Display multi-language attribute values.
 */
public class NamedAttributePanel extends FlowPanel {

  private String name;

  private JsArray<AttributeDto> attributes;

  public NamedAttributePanel() {
    addStyleName("well");
  }

  public void setName(String name) {
    this.name = name;
  }

  public JsArray<AttributeDto> getAttributes() {
    return attributes;
  }

  /**
   * Extract values of interest (requires at least name to be set).
   *
   * @param attributesArray
   */
  public void initialize(JsArray<AttributeDto> attributesArray, String missingMsg) {
    clear();
    attributes = JsArrays.create();
    for (AttributeDto attributeDto : JsArrays.toIterable(attributesArray)) {
      if (attributeDto.getName().equals(name)) {
        attributes.push(attributeDto);
        FlowPanel attrPanel = new FlowPanel();
        attrPanel.addStyleName("attribute-value");
        String value = attributeDto.getValue();
        if (attributeDto.hasLocale()) {
          Label localeLabel = new Label(attributeDto.getLocale());
          localeLabel.addStyleName("xsmall-right-indent");
          attrPanel.add(localeLabel);
        }
        attrPanel.add(new InlineLabel(value));
        add(attrPanel);
      }
    }
    if (attributes.length() == 0) {
      HelpBlock missingBlock = new HelpBlock(missingMsg);
      missingBlock.addStyleName("no-bottom-margin");
      add(missingBlock);
    }
  }
}
