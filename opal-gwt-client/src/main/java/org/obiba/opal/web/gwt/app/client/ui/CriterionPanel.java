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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.github.gwtbootstrap.client.ui.InputAddOn;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;

public class CriterionPanel extends FlowPanel {

  protected final Translations translations = GWT.create(Translations.class);

  private final CriterionDropdown criterion;

  public CriterionPanel(CriterionDropdown criterion) {
    this.criterion = criterion;
    initPanel(true, true);
  }

  public CriterionPanel(CriterionDropdown criterion, boolean removeable, boolean opened) {
    this.criterion = criterion;
    initPanel(removeable, opened);
  }

  private void initPanel(boolean removeable, boolean opened) {
    if(opened) {
      criterion.addStyleName("open");
    }

    InputAddOn w = new InputAddOn();
    w.addStyleName("small-addon");

    if(removeable) {
      w.addStyleName("input-append");
      w.addStyleName("no-bottom-margin");
      IconAnchor remove = new IconAnchor();
      remove.setIcon(IconType.REMOVE);
      remove.setTitle(translations.removeLabel());
      remove.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          removeFromParent();
          criterion.doFilterValueSets();
        }
      });
      w.addAppendWidget(remove);
    }

    w.add(criterion);
    add(w);

    addStyleName("xsmall-right-indent");
    addStyleName("inline-block");
  }

  public String getQueryString() {
    return criterion.getQueryString();
  }

  public String getQueryText() {
    return criterion.getText();
  }
}

