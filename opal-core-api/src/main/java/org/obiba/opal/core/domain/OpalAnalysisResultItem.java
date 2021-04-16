/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain;

import org.obiba.opal.spi.analysis.AnalysisResultItem;
import org.obiba.opal.spi.analysis.AnalysisStatus;

public class OpalAnalysisResultItem implements AnalysisResultItem {

  private String message;
  private AnalysisStatus status;

  OpalAnalysisResultItem() { }

  OpalAnalysisResultItem(AnalysisResultItem copy) {
    this.message = copy.getMessage();
    this.status = copy.getStatus();
  }

  @Override
  public AnalysisStatus getStatus() {
    return status;
  }

  @Override
  public String getMessage() {
    return message;
  }
}