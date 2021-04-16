/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.datashield.expr.web.support;

import org.junit.Test;
import org.obiba.opal.web.datashield.support.DataShieldPackageMethodHelper;
import org.obiba.opal.web.model.DataShield;

import java.util.Collection;
import java.util.Iterator;

import static org.fest.assertions.api.Assertions.assertThat;

public class DataShieldPackageMethodHelperTest {

  @Test
  public void test_methods() {
    Collection<DataShield.DataShieldMethodDto> dtos = DataShieldPackageMethodHelper.parsePackageMethods("dsBase", "1.0", ",colnames, ls=base::ls, ");
    assertThat(dtos.size()).isEqualTo(2);
    Iterator<DataShield.DataShieldMethodDto> iterator = dtos.iterator();

    DataShield.DataShieldMethodDto dto = iterator.next();
    assertThat(dto.getName()).isEqualTo("colnames");
    DataShield.RFunctionDataShieldMethodDto funcDto = dto.getExtension(DataShield.RFunctionDataShieldMethodDto.method);
    assertThat(funcDto.getFunc()).isEqualTo("dsBase::colnames");
    assertThat(funcDto.getRPackage()).isEqualTo("dsBase");
    assertThat(funcDto.getVersion()).isEqualTo("1.0");

    dto = iterator.next();
    assertThat(dto.getName()).isEqualTo("ls");
    funcDto = dto.getExtension(DataShield.RFunctionDataShieldMethodDto.method);
    assertThat(funcDto.getFunc()).isEqualTo("base::ls");
    assertThat(funcDto.getRPackage()).isEqualTo("dsBase");
    assertThat(funcDto.getVersion()).isEqualTo("1.0");
  }
}
