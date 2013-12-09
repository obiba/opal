/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.taxonomy;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.web.model.Opal.TaxonomyDto;
import org.obiba.opal.web.taxonomy.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/system/conf/taxonomies")
public class TaxonomiesResource {

  @Autowired
  private TaxonomyService taxonomyService;

  @GET
  public List<TaxonomyDto> getTaxonomies() {
    List<TaxonomyDto> taxonomies = new ArrayList<TaxonomyDto>();
    for(Taxonomy taxonomy : taxonomyService.getTaxonomies()) {
      taxonomies.add(Dtos.asDto(taxonomy));
    }
    return taxonomies;
  }

  @POST
  public Response addTaxonomy(TaxonomyDto dto) {
    taxonomyService.saveTaxonomy(null, Dtos.fromDto(dto));
    return Response.ok().build();
  }
}

