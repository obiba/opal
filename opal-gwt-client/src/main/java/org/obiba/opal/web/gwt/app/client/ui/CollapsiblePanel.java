/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class CollapsiblePanel extends SimplePanel {

  public enum HeadingType {
    DETAILS,
    ADVANCED_OPTIONS
  }

  private static final Translations translations = GWT.create(Translations.class);

  private final DisclosurePanel disclosurePanel;

  private final Label header;

  private final Icon icon;

  private final FlowPanel content;

  public CollapsiblePanel() {
    this(HeadingType.ADVANCED_OPTIONS);
  }

  public CollapsiblePanel(HeadingType headingType) {
    this(translations.collapsibleMap().get(headingType.name()));
  }

  public CollapsiblePanel(String heading) {
    disclosurePanel = new DisclosurePanel();

    FlowPanel h = new FlowPanel();
    h.add(icon = new Icon(IconType.PLUS));
    h.add(header = new InlineLabel(heading));
    header.addStyleName("small-indent");
    setHeader(h);

    disclosurePanel.addCloseHandler(new CloseHandler<DisclosurePanel>() {
      @Override
      public void onClose(CloseEvent<DisclosurePanel> event) {
        icon.setIcon(IconType.PLUS);
      }
    });

    disclosurePanel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
      @Override
      public void onOpen(OpenEvent<DisclosurePanel> event) {
        icon.setIcon(IconType.MINUS);
      }
    });

    disclosurePanel.add(content = new FlowPanel());
    disclosurePanel.removeStyleName("gwt-DisclosurePanel");
    disclosurePanel.setAnimationEnabled(true);
    addStyleName("collapsible");
    setWidget(disclosurePanel);
  }

  public void setHeading(HeadingType type) {
    header.setText(translations.collapsibleMap().get(type.name()));
  }

  public void setText(String heading) {
    header.setText(heading);
  }

  /**
   * Sets the widget used as the header for the panel.
   *
   * @param headerWidget the widget to be used as the header
   */
  public void setHeader(Widget headerWidget) {
    disclosurePanel.setHeader(headerWidget);
  }

  @Override
  public void add(Widget w) {
    content.add(w);
  }

  @Override
  public void add(IsWidget child) {
    content.add(child);
  }

  public void setOpen(boolean isOpen) {
    disclosurePanel.setOpen(isOpen);
  }
}
