/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.jdbc;

import org.obiba.opal.core.runtime.jdbc.JdbcDataSource;
import org.obiba.opal.web.model.Opal.JdbcDataSourceDto;

import com.google.common.base.Function;
import com.google.common.base.Strings;

final class Dtos {

  static final class JdbcDataSourceDtos {
    static final Function<JdbcDataSource, JdbcDataSourceDto> asDto = new Function<JdbcDataSource, JdbcDataSourceDto>() {

      @Override
      public JdbcDataSourceDto apply(JdbcDataSource input) {
        return JdbcDataSourceDto.newBuilder()//
            .setName(input.getName())//
            .setDriverClass(input.getDriverClass())//
            .setUrl(input.getUrl())//
            .setUsername(input.getUsername())//
                // Do not send password.
                // .setPassword(Strings.nullToEmpty(input.getPassword()))//
            .setEditable(input.isEditable()) //
            .setProperties(Strings.nullToEmpty(input.getProperties())).build();
      }

    };

    static final Function<JdbcDataSourceDto, JdbcDataSource> fromDto
        = new Function<JdbcDataSourceDto, JdbcDataSource>() {

      @Override
      public JdbcDataSource apply(JdbcDataSourceDto dto) {
        return new JdbcDataSource(dto.getName(), dto.getUrl(), dto.getDriverClass(), dto.getUsername(),
            dto.getPassword(), dto.getProperties());
      }

    };

    private JdbcDataSourceDtos() {}
  }

}
