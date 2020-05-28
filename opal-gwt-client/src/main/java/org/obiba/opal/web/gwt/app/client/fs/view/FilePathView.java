/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FilePathPresenter;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewImpl;

public class FilePathView extends ViewImpl implements FilePathPresenter.Display {

  private final EventBus eventBus;

  private final Breadcrumbs filecrumbs;

  @Inject
  public FilePathView(EventBus eventBus) {
    filecrumbs = new Breadcrumbs();
    this.eventBus = eventBus;
  }

  @Override
  public Widget asWidget() {
    return filecrumbs;
  }

  @Override
  public void setFile(FileDto file) {
    filecrumbs.clear();
    List<FileDto> parents = FileDtos.getParents(file);
    if(parents.isEmpty()) {
      NavLink link = new NavLink();
      link.setIcon(IconType.HDD);
      filecrumbs.add(link);
      // need this otherwise root icon does not show up
      filecrumbs.add(new NavLink());
    } else {
      for(FileDto parent : parents) {
        addFileLink(parent);
      }
      addFileLink(file);
    }
  }

  private void addFileLink(final FileDto file) {
    NavLink link = new NavLink(file.getName());
    if(Strings.isNullOrEmpty(file.getName())) {
      link.setIcon(IconType.HDD);
    }
    filecrumbs.add(link);
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new FolderRequestEvent(file));
      }
    });
  }
}
