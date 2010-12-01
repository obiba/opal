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

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.obiba.magma.views.View;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

public class ViewResource extends AbstractValueTableResource {
  //
  // Constructors
  //

  public ViewResource(View view, Set<Locale> locales) {
    super(view, locales);
  }

  public ViewResource(View view) {
    this(view, Collections.<Locale> emptySet());
  }

  //
  // Methods
  //

  @GET
  public ViewDto getView() {
    return ViewDtos.asDto(asView());
  }

  @GET
  @Produces("application/xml")
  public View getViewInstance() {
    return asView();
  }

  @Path("/from")
  @Bean
  @Scope("request")
  public TableResource getFrom() {
    return new TableResource(asView().getWrappedValueTable(), getLocales());
  }

  protected View asView() {
    return (View) getValueTable();
  }
}
