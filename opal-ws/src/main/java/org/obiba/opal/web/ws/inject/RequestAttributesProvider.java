/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.ws.inject;

import java.net.URI;

import javax.ws.rs.core.UriInfo;

import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 */
public interface RequestAttributesProvider {

  ServletRequestAttributes currentRequestAttributes();

  UriInfo getUriInfo();

  String getResourcePath(URI uri);
}
