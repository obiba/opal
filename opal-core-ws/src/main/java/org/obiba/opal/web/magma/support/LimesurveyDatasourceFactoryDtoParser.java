/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

import javax.annotation.Nonnull;

import org.obiba.magma.DatasourceFactory;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.LimesurveyDatasourceFactoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LimesurveyDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  private final DatabaseRegistry databaseRegistry;

  @Autowired
  public LimesurveyDatasourceFactoryDtoParser(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

  @Nonnull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto) {
    LimesurveyDatasourceFactoryDto limesurveyDto = dto.getExtension(LimesurveyDatasourceFactoryDto.params);
    return new DatabaseLimesurveyDatasourceFactory(dto.getName(), //
        limesurveyDto.getDatabase(), //
        databaseRegistry);
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(LimesurveyDatasourceFactoryDto.params);
  }

}
