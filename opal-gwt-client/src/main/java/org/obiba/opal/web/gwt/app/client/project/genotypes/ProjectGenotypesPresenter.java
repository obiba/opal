/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.genotypes;

import com.google.gwt.core.client.JsArray;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.VCFSummaryDto;

public class ProjectGenotypesPresenter extends PresenterWidget<ProjectGenotypesPresenter.Display>
    implements ProjectGenotypesUiHandlers {


  @Inject
  public ProjectGenotypesPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
  }

  public void initialize(ProjectDto projectDto) {
  }

  @Override
  public void onDownloadVcfFiles() {

  }

  @Override
  public void onImportVcfFiles() {

  }

  @Override
  public void onRemoveVcfFile(VCFSummaryDto vcfSummaryDto) {

  }

  @Override
  public void onDownloadVcfFile(VCFSummaryDto vcfSummaryDto) {

  }

  @Override
  public void onDownloadStatistics(VCFSummaryDto vcfSummaryDto) {

  }

  public interface Display extends View, HasUiHandlers<ProjectGenotypesUiHandlers> {
    void renderRows(JsArray<VCFSummaryDto> rows);
  }
}
