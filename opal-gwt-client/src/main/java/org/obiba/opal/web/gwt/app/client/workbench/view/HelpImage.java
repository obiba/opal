/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import java.util.Iterator;

import org.obiba.opal.web.gwt.app.client.presenter.HelpUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * This image can be automatically link to a Opal Documentation Wiki page.
 */
public class HelpImage extends Image implements HasWidgets {

  private Tooltip tooltip;

  private String tooltipHeight = "250px";

  private String tooltipWidth = "300px";

  public HelpImage() {
    setImageSize(20);
    addStyleName("help");
  }

  public void setImageSize(int size) {
    setUrl("image/" + size + "/help.png");
  }

  public void setPage(final String page) {
    addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        HelpUtil.openPage(page);
      }
    });
    setTitle(page.replace('+', ' '));
  }

  private Tooltip getTooltip() {
    if(tooltip == null) {
      tooltip = new Tooltip();
      addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent evt) {
          tooltip.setPopupPosition(evt.getNativeEvent().getClientX(), evt.getNativeEvent().getClientY());
          tooltip.setSize(tooltipWidth, tooltipHeight);
          tooltip.show();
        }
      });
    }
    return tooltip;
  }

  public void setTooltipHeight(String tooltipHeight) {
    this.tooltipHeight = tooltipHeight;
  }

  public void setTooltipWidth(String tooltipWidth) {
    this.tooltipWidth = tooltipWidth;
  }

  @Override
  public void add(Widget w) {
    getTooltip().add(w);
  }

  @Override
  public void clear() {
    getTooltip().clear();
  }

  @Override
  public Iterator<Widget> iterator() {
    return getTooltip().iterator();
  }

  @Override
  public boolean remove(Widget w) {
    return getTooltip().remove(w);
  }

}
