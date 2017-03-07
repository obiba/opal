/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.vcf;

import org.obiba.opal.spi.vcf.VCFStore;
import org.obiba.opal.web.model.Plugins;

public class Dtos {

  public static Plugins.VCFStoreDto asDto(VCFStore store) {
    Plugins.VCFStoreDto.Builder builder = Plugins.VCFStoreDto.newBuilder();
    builder.setName(store.getName()) //
        .setSamplesCount(store.getSampleIds().size()) //
        .addAllVcf(store.getVCFNames());
    return builder.build();
  }

  public static Plugins.VCFSummaryDto asDto(VCFStore.VCFSummary summary) {
    Plugins.VCFSummaryDto.Builder builder = Plugins.VCFSummaryDto.newBuilder();
    builder.setName(summary.getName()) //
        .setSize(summary.size()) //
        .setSamplesCount(summary.getSampleIds().size()) //
        .setGenotypesCount(summary.getGenotypesCount()) //
        .setVariantsCount(summary.getVariantsCount());
    return builder.build();
  }

}
