/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.magma.event.VcsCommitInfoReceivedEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.datetime.client.FormatType;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.VcsCommitInfoDto;
import org.obiba.opal.web.model.client.opal.VcsCommitInfosDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.UiHandlers;
import com.gwtplatform.mvp.client.View;

public class VariableVcsCommitHistoryPresenter extends PresenterWidget<VariableVcsCommitHistoryPresenter.Display>
    implements UiHandlers {

  public interface Display extends View, HasUiHandlers<UiHandlers> {
    String DIFF_ACTION = "CommitDiff";
    String DIFF_CURRENT_ACTION = "DiffWithCurrent";

    void setData(JsArray<VcsCommitInfoDto> commitInfos);

    HasActionHandler<VcsCommitInfoDto> getActions();
  }

  static final String HEAD_REVISION = "head";

  private final ModalProvider<VcsCommitHistoryModalPresenter> vcsHistoryModalProvider;

  private final Translations translations;

  private TableDto table;

  private VariableDto variable;

  private VcsCommitHistoryModalPresenter vcsHistoryModalPresenter;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public VariableVcsCommitHistoryPresenter(Display display, EventBus eventBus,
      ModalProvider<VcsCommitHistoryModalPresenter> vcsHistoryModalProvider, Translations translations) {
    super(eventBus, display);
    this.vcsHistoryModalProvider = vcsHistoryModalProvider.setContainer(this);
    this.translations = translations;
    getView().setUiHandlers(this);
  }

  public void retrieveCommitInfos(TableDto tableDto, VariableDto variableDto) {
    if(tableDto == null || variableDto == null) return;

    table = tableDto;
    variable = variableDto;

    String requestUri = UriBuilders.VCS_VARIABLE_COMMIT_INFOS.create()
        .build(table.getDatasourceName(), table.getName(), variable.getName());

    ResourceRequestBuilderFactory.<VcsCommitInfosDto>newBuilder()//
        .forResource(requestUri).withCallback(new ResourceCallback<VcsCommitInfosDto>() {
      @Override
      public void onResource(Response response, VcsCommitInfosDto commitInfos) {
        getView().setData(commitInfos.getCommitInfosArray());
      }
    }).get().send();
  }

  @Override
  protected void onBind() {
    super.onBind();

    getView().getActions().setActionHandler(new VcsCommitInfoActionHandler());
  }

  private class VcsCommitInfoActionHandler implements ActionHandler<VcsCommitInfoDto> {

    @Override
    public void doAction(VcsCommitInfoDto commitInfo, String actionName) {
        if(ActionsColumn.EDIT_ACTION.equals(actionName)) {
          viewCommitContent(commitInfo);
        } else if(Display.DIFF_ACTION.equals(actionName)) {
          showCommitInfo(commitInfo, false);
        } else if(Display.DIFF_CURRENT_ACTION.equals(actionName)) {
          showCommitInfo(commitInfo, true);
        }
    }

    private void showCommitInfo(VcsCommitInfoDto dto, boolean withCurrent) {
      String requestUri = UriBuilders.VCS_VARIABLE_COMMIT_INFO.create()
          .build(table.getDatasourceName(), table.getName(), variable.getName(), withCurrent ? HEAD_REVISION : "",
              dto.getCommitId());

      ResourceRequestBuilderFactory.<VcsCommitInfoDto>newBuilder()//
          .forResource(requestUri).withCallback(new ResourceCallback<VcsCommitInfoDto>() {
        @Override
        public void onResource(Response response, VcsCommitInfoDto resource) {
          vcsHistoryModalProvider.get().setCommitInfo(resource);
        }
      }).get().send();
    }

    private void viewCommitContent(VcsCommitInfoDto dto) {
      Moment m = Moment.create(dto.getDate());
      String age = TranslationsUtils
          .replaceArguments(translations.momentWithAgo(), m.format(FormatType.MONTH_NAME_TIME_SHORT), m.fromNow());

      getEventBus()
          .fireEvent(NotificationEvent.newBuilder().info("VcsScriptContentInfo").args(age, dto.getAuthor()).build());

      String requestUri = UriBuilders.VCS_VARIABLE_BLOB.create()
          .build(table.getDatasourceName(), table.getName(), variable.getName(), dto.getCommitId());

      ResourceRequestBuilderFactory.<VcsCommitInfoDto>newBuilder()//
          .forResource(requestUri).withCallback(new ResourceCallback<VcsCommitInfoDto>() {
        @Override
        public void onResource(Response response, VcsCommitInfoDto resource) {
          getEventBus().fireEvent(new VcsCommitInfoReceivedEvent(resource));
        }
      }).get().send();
    }

  }
}
