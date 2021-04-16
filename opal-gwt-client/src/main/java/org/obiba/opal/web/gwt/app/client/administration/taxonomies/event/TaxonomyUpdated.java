/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.event;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Will generate {@link org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent} and {@link org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent.TaxonomyCreatedHandler}
 */

@GenEvent
public class TaxonomyUpdated {

  String name;

}
