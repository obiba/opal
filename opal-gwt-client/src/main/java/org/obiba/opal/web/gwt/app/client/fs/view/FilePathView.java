/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.view;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FilePathPresenter;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class FilePathView extends ViewImpl implements FilePathPresenter.Display {

  private final Breadcrumbs filecrumbs;

  @Inject
  public FilePathView() {
    filecrumbs = new Breadcrumbs();
  }

  @Override
  public Widget asWidget() {
    return filecrumbs;
  }

  @Override
  public void setFile(FileDto file) {
    String[] segments = file.getPath().split("/");
    filecrumbs.clear();
    if(segments.length == 0) {
      NavLink link = new NavLink();
      link.setIcon(IconType.HDD);
      filecrumbs.add(link);
      // need this otherwise root icon does not show up
      filecrumbs.add(new NavLink());
    } else {
      for(String segment : segments) {
        NavLink link = new NavLink(segment);
        if(Strings.isNullOrEmpty(segment)) {
          link.setIcon(IconType.HDD);
        }
        filecrumbs.add(link);
        link.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            // TODO UiHandler callback
          }
        });
      }
    }
  }
}
