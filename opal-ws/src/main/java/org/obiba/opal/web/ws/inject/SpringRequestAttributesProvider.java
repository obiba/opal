/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.ws.inject;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SpringRequestAttributesProvider implements RequestAttributesProvider {

  @Override
  public ServletRequestAttributes currentRequestAttributes() {
    RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
    if(attributes instanceof ServletRequestAttributes) {
      return (ServletRequestAttributes) attributes;
    }
    throw new IllegalStateException("Not a servlet request");
  }

}
