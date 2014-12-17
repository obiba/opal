package org.obiba.opal.web.gwt.app.client.administration.taxonomies.event;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Will generate {@link org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent} and {@link org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent.TaxonomyCreatedHandler}
 */

@GenEvent
public class TaxonomyUpdated {

  String name;

}
