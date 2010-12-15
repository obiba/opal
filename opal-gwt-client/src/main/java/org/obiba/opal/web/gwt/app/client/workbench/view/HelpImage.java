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

import org.obiba.opal.web.gwt.app.client.presenter.HelpUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;

/**
 * This image can be automatically link to a Opal Documentation Wiki page.
 */
public class HelpImage extends Image {

  public HelpImage() {
    setImageUrl(20);
    addStyleName("help");
  }

  public void setImageUrl(int size) {
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

}
