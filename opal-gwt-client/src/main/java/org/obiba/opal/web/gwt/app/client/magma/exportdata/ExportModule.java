/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.exportdata;

import org.obiba.opal.web.gwt.app.client.inject.AbstractOpalModule;
import org.obiba.opal.web.gwt.app.client.magma.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.magma.copydata.view.DataCopyView;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.view.DataExportView;

/**
 * Bind concrete implementations to interfaces within the export wizard.
 */
public class ExportModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindPresenterWidget(DataExportPresenter.class, DataExportPresenter.Display.class, DataExportView.class);
    bindPresenterWidget(DataCopyPresenter.class, DataCopyPresenter.Display.class, DataCopyView.class);
  }
}
