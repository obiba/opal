/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project;

import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectPresenter;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public class ProjectPlacesHelper {

  private ProjectPlacesHelper() {
  }

  public static PlaceRequest getProjectPlace(String project) {
    return new PlaceRequest.Builder().nameToken(Places.PROJECT) //
        .with(ParameterTokens.TOKEN_NAME, project) //
        .build();
  }

  public static PlaceRequest getAdministrationPlace(String project) {
    return new PlaceRequest.Builder().nameToken(Places.PROJECT) //
        .with(ParameterTokens.TOKEN_NAME, project)  //
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.ADMINISTRATION.toString()) //
        .build();
  }

  public static PlaceRequest getDatasourcePlace(String project) {
    return new PlaceRequest.Builder().nameToken(Places.PROJECT) //
        .with(ParameterTokens.TOKEN_NAME, project)  //
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.TABLES.toString()) //
        .build();
  }

  public static PlaceRequest getTablePlace(String datasource, String table) {
    return new PlaceRequest.Builder().nameToken(Places.PROJECT) //
        .with(ParameterTokens.TOKEN_NAME, datasource) //
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.TABLES.toString()) //
        .with(ParameterTokens.TOKEN_PATH, datasource + "." + table) //
        .build();
  }

  public static PlaceRequest getVariablePlace(String datasource, String table, String variable) {
    return new PlaceRequest.Builder().nameToken(Places.PROJECT) //
        .with(ParameterTokens.TOKEN_NAME, datasource) //
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.TABLES.toString()) //
        .with(ParameterTokens.TOKEN_PATH, datasource + "." + table + ":" + variable) //
        .build();
  }

  public static PlaceRequest getTablesPlace(String project) {
    return new PlaceRequest.Builder().nameToken(Places.PROJECT) //
        .with(ParameterTokens.TOKEN_NAME, project) //
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.TABLES.toString()) //
        .build();
  }

  public static PlaceRequest getReportsPlace(String project) {
    return new PlaceRequest.Builder().nameToken(Places.PROJECT) //
        .with(ParameterTokens.TOKEN_NAME, project) //
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.REPORTS.toString()) //
        .build();
  }

}
