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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;

import org.obiba.opal.web.model.Opal.LocaleDto;
import org.obiba.opal.web.ws.security.NoAuthorization;

/**
 *
 */
@NoAuthorization
public class LocalesResource {

  private final Set<Locale> locales;

  public LocalesResource(Set<Locale> locales) {
    this.locales = new LinkedHashSet<Locale>();
    this.locales.addAll(locales);
  }

  @GET
  public Iterable<LocaleDto> getLocales(@QueryParam("locale") String displayLocale) {
    List<LocaleDto> localeDtos = new ArrayList<LocaleDto>();
    for(Locale locale : locales) {
      localeDtos.add(Dtos.asDto(locale, displayLocale != null ? new Locale(displayLocale) : null));
    }

    return localeDtos;
  }

}
