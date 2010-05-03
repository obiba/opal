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

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
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
public class TableResource {

  @RequestMapping(value = "/datasource/{datasource}/{table}/variables", method = RequestMethod.GET)
  public ResponseEntity<?> getVariables(@PathVariable String datasource, @PathVariable String table) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    return new ResponseEntity<Object>(MagmaEngine.get().getDatasource(datasource).getValueTable(table).getVariables(), headers, HttpStatus.OK);
  }

  @RequestMapping(value = "/datasource/{datasource}/{table}/entities", method = RequestMethod.GET)
  public ResponseEntity<?> getEntities(@PathVariable String datasource, @PathVariable String table) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    Set<String> identifiers = ImmutableSet.copyOf(Iterables.transform(MagmaEngine.get().getDatasource(datasource).getValueTable(table).getValueSets(), new Function<ValueSet, String>() {
      @Override
      public String apply(ValueSet from) {
        return from.getVariableEntity().getIdentifier();
      }
    }));
    return new ResponseEntity<Object>(identifiers, headers, HttpStatus.OK);
  }
}
