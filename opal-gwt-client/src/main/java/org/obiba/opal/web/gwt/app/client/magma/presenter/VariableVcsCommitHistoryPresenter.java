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

import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.VcsCommitInfoDto;
import org.obiba.opal.web.model.client.opal.VcsCommitInfosDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class VariableVcsCommitHistoryPresenter extends PresenterWidget<VariableVcsCommitHistoryPresenter.Display>
    implements VariableVcsCommitHistoryUiHandlers {

  public interface Display extends View, HasUiHandlers<VariableVcsCommitHistoryUiHandlers> {
    void setData(JsArray<VcsCommitInfoDto> commitInfos);
  }

  private final ModalProvider<VcsCommitHistoryModalPresenter> vcsHistoryModalProvider;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public VariableVcsCommitHistoryPresenter(Display display, EventBus eventBus,
      ModalProvider<VcsCommitHistoryModalPresenter> vcsHistoryModalProvider) {
    super(eventBus, display);
    this.vcsHistoryModalProvider = vcsHistoryModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  public void showCommitInfo(VcsCommitInfoDto dto) {
    VcsCommitHistoryModalPresenter vcsHistoryModalPresenter = vcsHistoryModalProvider.get();
    vcsHistoryModalPresenter.setCommitInfo(dto);
  }

  public void retrieveCommitInfos(TableDto table, VariableDto variable) {
    if (table == null || variable == null) {
      throw new NullPointerException("Table and variable cannot be null");
    }

    String requestUri = UriBuilder.create()
        .segment("datasource", table.getDatasourceName(), "view", table.getName(), "vcs", "variable",
            variable.getName(), "commits").build();

    ResourceRequestBuilderFactory.<VcsCommitInfosDto>newBuilder()//
        .forResource(requestUri).withCallback(new ResourceCallback<VcsCommitInfosDto>() {
      @Override
      public void onResource(Response response, VcsCommitInfosDto commitInfos) {
        getView().setData(commitInfos.getCommitInfosArray());
      }
    }).get().send();
  }

}
