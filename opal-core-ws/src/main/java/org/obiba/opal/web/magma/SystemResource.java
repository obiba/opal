/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.obiba.opal.web.model.Opal.JdbcDriverDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Path("/system")
public class SystemResource {
  //
  // Instance Variables
  //

  @Autowired
  private @Value("${org.obiba.opal.jdbc.drivers}")
  String jdbcDriversProperty;

  private Map<String, String> jdbcDrivers;

  //
  // Methods
  //

  @GET
  @Path("/jdbcDrivers")
  public Iterable<JdbcDriverDto> getJdbcDrivers() {
    List<JdbcDriverDto> driverDtos = new ArrayList<JdbcDriverDto>();

    for(Map.Entry<String, String> driver : getJdbcDriverMap().entrySet()) {
      JdbcDriverDto driverDto = JdbcDriverDto.newBuilder().setDriverName(driver.getKey()).setDriverClass(driver.getValue()).build();
      driverDtos.add(driverDto);
    }

    return driverDtos;
  }

  private Map<String, String> getJdbcDriverMap() {
    if(jdbcDrivers == null) {
      jdbcDrivers = new LinkedHashMap<String, String>();

      String[] drivers = jdbcDriversProperty.split(",");
      for(String driver : drivers) {
        String[] driverNameAndClass = driver.split("=");
        if(driverNameAndClass.length == 2) {
          String driverName = driverNameAndClass[0].trim();
          String driverClass = driverNameAndClass[1].trim();
          jdbcDrivers.put(driverName, driverClass);
        }
      }
    }
    return jdbcDrivers;
  }
}
