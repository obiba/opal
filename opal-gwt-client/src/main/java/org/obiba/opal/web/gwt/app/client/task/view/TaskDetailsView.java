/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.task.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.common.BaseMessagesView;
import org.obiba.opal.web.gwt.app.client.task.presenter.TaskDetailsPresenter.Display;
import org.obiba.opal.web.model.client.opal.CommandStateDto;

public class TaskDetailsView extends BaseMessagesView implements Display {

  interface ViewUiBinder extends UiBinder<Widget, TaskDetailsView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @Inject
  public TaskDetailsView(EventBus eventBus) {
    super(eventBus);
  }

  @Override
  public void setJob(CommandStateDto commandStateDto) {
    dialogBox.setTitle(translations.taskLabel() + " #" + commandStateDto.getId());
    setData(commandStateDto.getMessagesArray());
  }

    @Override
    protected Widget createWidget() {
        return uiBinder.createAndBindUi(this);
    }

}
