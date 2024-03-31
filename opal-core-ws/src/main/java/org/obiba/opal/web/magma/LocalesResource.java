/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;

import org.obiba.opal.web.model.Opal.LocaleDto;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NoAuthorization
public class LocalesResource {

  private Set<Locale> locales;

  public void setLocales(Set<Locale> locales) {
    this.locales = locales;
  }

  @GET
  public Iterable<LocaleDto> getLocales(@QueryParam("locale") String displayLocale) {
    Collection<LocaleDto> localeDtos = new ArrayList<>();
    for(Locale locale : locales) {
      localeDtos.add(Dtos.asDto(locale, displayLocale != null ? new Locale(displayLocale) : null));
    }
    return localeDtos;
  }

}
