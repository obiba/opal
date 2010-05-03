/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.spring;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Controller
public class DatasourceResource {

  @RequestMapping(value = "/datasources", method = RequestMethod.GET)
  public ResponseEntity<Set<String>> getDatasources() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    Set<String> names = ImmutableSet.copyOf(Iterables.transform(MagmaEngine.get().getDatasources(), new Function<Datasource, String>() {
      @Override
      public String apply(Datasource from) {
        return from.getName();
      }
    }));
    return new ResponseEntity<Set<String>>(names, headers, HttpStatus.OK);
  }

  @RequestMapping(value = "/datasource/{name}/tables", method = RequestMethod.GET)
  public ResponseEntity<Set<String>> getDatasource(@PathVariable String name) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    Set<String> names = ImmutableSet.copyOf(Iterables.transform(MagmaEngine.get().getDatasource(name).getValueTables(), new Function<ValueTable, String>() {
      @Override
      public String apply(ValueTable from) {
        return from.getName();
      }
    }));
    return new ResponseEntity<Set<String>>(names, headers, HttpStatus.OK);
  }

}
