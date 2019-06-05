/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.identifiers.IdentifiersMappingDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

public class IdentifiersMappingSelectionStepPresenter
    extends PresenterWidget<IdentifiersMappingSelectionStepPresenter.Display> {

  @Inject
  public IdentifiersMappingSelectionStepPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    super.onBind();
    initIdentifiersMappings();
  }

  private void initIdentifiersMappings() {

    ResourceRequestBuilderFactory.<JsArray<IdentifiersMappingDto>>newBuilder().forResource(UriBuilders.IDENTIFIERS_MAPPINGS.create().build())
        .get().withCallback(new ResourceCallback<JsArray<IdentifiersMappingDto>>() {
      @Override
      public void onResource(Response response, JsArray<IdentifiersMappingDto> resource) {
        getView().setIdentifiersMappings(JsArrays.toSafeArray(resource));
      }

    }).withCallback(Response.SC_FORBIDDEN, ResponseCodeCallback.NO_OP).send();
  }

  public void initializeDefaultIdentifiersMapping(String project, String entityType) {
    if (Strings.isNullOrEmpty(entityType)) {
      String uri = UriBuilders.PROJECT_IDENTIFIERS_MAPPINGS.create().build(project);
      ResourceRequestBuilderFactory.<JsArray<ProjectDto.IdentifiersMappingDto>>newBuilder().forResource(uri)
        .get().withCallback(new ResourceCallback<JsArray<ProjectDto.IdentifiersMappingDto>>() {
        @Override
        public void onResource(Response response, JsArray<ProjectDto.IdentifiersMappingDto> mappings) {
          if (mappings.length() > 0) getView().selectIdentifiersMapping(mappings.get(0));
        }

      }).withCallback(Response.SC_FORBIDDEN, ResponseCodeCallback.NO_OP).send();
    } else {

      String uri = UriBuilders.PROJECT_IDENTIFIERS_MAPPING.create().query("entityType", entityType).build(project);
      ResourceRequestBuilderFactory.<ProjectDto.IdentifiersMappingDto>newBuilder().forResource(uri)
        .get().withCallback(new ResourceCallback<ProjectDto.IdentifiersMappingDto>() {
        @Override
        public void onResource(Response response, ProjectDto.IdentifiersMappingDto mapping) {
          getView().selectIdentifiersMapping(mapping);
        }

      }).withCallback(Response.SC_FORBIDDEN, ResponseCodeCallback.NO_OP).send();
    }
  }

  public void updateImportConfig(ImportConfig importConfig) {
    boolean withUnit = getView().getSelectedIdentifiersMapping() != null;
    importConfig.setIdentifierSharedWithUnit(withUnit);
    importConfig.setIdentifierAsIs(!withUnit);
    importConfig.setIdentifiersMapping(getView().getSelectedIdentifiersMapping());
    importConfig.setIncremental(getView().isIncremental());
    importConfig.setLimit(getView().getLimit());
    //GWT.log("ignore=" + getView().ignoreUnknownIdentifier() + " ; allow=" + getView().allowIdentifierGeneration());
    importConfig.setAllowIdentifierGeneration(getView().allowIdentifierGeneration());
    importConfig.setIgnoreUnknownIdentifier(getView().ignoreUnknownIdentifier());
  }

  public interface Display extends View {

    void setIdentifiersMappings(JsArray<IdentifiersMappingDto> mappings);

    void selectIdentifiersMapping(ProjectDto.IdentifiersMappingDto mapping);

    String getSelectedIdentifiersMapping();

    boolean isIncremental();

    Integer getLimit();

    boolean allowIdentifierGeneration();

    boolean ignoreUnknownIdentifier();

  }

}
