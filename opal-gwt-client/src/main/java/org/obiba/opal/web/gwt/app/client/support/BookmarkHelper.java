/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import javax.validation.constraints.NotNull;

import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.model.client.opal.LinkDto;

import com.google.common.base.Preconditions;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public final class BookmarkHelper {

  private BookmarkHelper() {}

  public static PlaceRequest createPlaceRequest(@NotNull LinkDto linkDto) {
    Preconditions.checkArgument(linkDto != null);
    return createPlaceRequest(linkDto.getRel());
  }

  public static PlaceRequest createPlaceRequest(@NotNull String path) {
    Preconditions.checkArgument(path != null);
    Tokenizer tokenizer = Tokenizer.newTokenizer().tokenize(path);
    if(tokenizer.hasVariable()) {
      return ProjectPlacesHelper
          .getVariablePlace(tokenizer.getProject(), tokenizer.getTable(), tokenizer.getVariable());
    } else if(tokenizer.hasTable()) {
      return ProjectPlacesHelper.getTablePlace(tokenizer.getProject(), tokenizer.getTable());
    } else if(tokenizer.hasProject()) {
      return ProjectPlacesHelper.getProjectPlace(tokenizer.getProject());
    }

    throw new IllegalArgumentException("LinkDto argument contains invalid resource link");
  }

  public static String createMagmaPath(@NotNull String path) {
    Preconditions.checkArgument(path != null);
    Tokenizer tokenizer = Tokenizer.newTokenizer().tokenize(path);
    return MagmaPath.Builder.datasource(tokenizer.getProject()).table(tokenizer.getTable())
        .variable(tokenizer.getVariable()).build();
  }

  private static class Tokenizer {

    public Tokenizer() {}

    private int index = 0;

    private String[] tokens;

    private String project;

    private String table;

    private String variable;

    public static Tokenizer newTokenizer() {
      return new Tokenizer();
    }

    public Tokenizer tokenize(String path) {

      tokens = path.substring(1).split("/");
      index = 0;

      while(hasToken()) {
        String resource = nextToken();
        if("datasource".equals(resource)) {
          project = nextToken();
        } else if("table".equals(resource)) {
          table = nextToken();
        } else if("variable".equals(resource)) {
          variable = nextToken();
        }
      }

      return this;
    }

    public String toString() {
      return "Project: " + project + " Table: " + table + " variable: " + variable;
    }

    public boolean hasProject() {
      return project != null;
    }

    public String getProject() {
      return project;
    }

    public boolean hasTable() {
      return table != null;
    }

    public String getTable() {
      return table;

    }

    public boolean hasVariable() {
      return variable != null;
    }

    public String getVariable() {
      return variable;
    }

    private boolean hasToken() {
      return index < tokens.length;
    }

    private String nextToken() {
      return tokens[index++];
    }
  }
}
