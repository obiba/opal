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

import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.Label;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.markdown.client.Markdown;
import org.obiba.opal.web.model.client.magma.AttributeDto;

/**
 * Display multi-language attribute values.
 */
public class NamedAttributePanel extends FlowPanel {

  private String name;

  private JsArray<AttributeDto> attributes;

  public NamedAttributePanel(String name) {
    this.name = name;
  }

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
        String value = attributeDto.getValue();
        if (!Strings.isNullOrEmpty(value)) {
          attributes.push(attributeDto);
          SafeHtmlBuilder builder = new SafeHtmlBuilder();
          if (attributeDto.hasLocale()) {
            builder.appendHtmlConstant("<span class=\"label xsmall-right-indent\" style=\"vertical-align: top\">" + attributeDto.getLocale() + "</span>");
          }
          builder.appendHtmlConstant(Markdown.parse(value));
          HTMLPanel attrPanel = new HTMLPanel(builder.toSafeHtml());
          attrPanel.addStyleName("attribute-value");
          add(attrPanel);
        }
      }
    }
    if (attributes.length() == 0 && !Strings.isNullOrEmpty(missingMsg)) {
      HelpBlock missingBlock = new HelpBlock(missingMsg);
      missingBlock.addStyleName("no-bottom-margin");
      add(missingBlock);
    }
  }
}
