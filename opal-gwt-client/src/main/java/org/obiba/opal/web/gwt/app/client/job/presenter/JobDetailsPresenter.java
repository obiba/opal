/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.job.presenter;

import org.obiba.opal.web.model.client.opal.CommandStateDto;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class JobDetailsPresenter extends PresenterWidget<JobDetailsPresenter.Display> {

  public interface Display extends PopupView {

    void setJob(CommandStateDto commandStateDto);

  }

  @Inject
  public JobDetailsPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  public void setJob(CommandStateDto dto) {
    getView().setJob(dto);
  }

}
