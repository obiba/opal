package org.obiba.opal.elmo;

import org.obiba.opal.elmo.concepts.CategoricalVariable;
import org.obiba.opal.elmo.concepts.Category;
import org.obiba.opal.elmo.concepts.ContinuousVariable;
import org.obiba.opal.elmo.concepts.DataItem;
import org.obiba.opal.elmo.concepts.DataVariable;
import org.obiba.opal.elmo.concepts.Dataset;
import org.obiba.opal.elmo.concepts.Entity;
import org.obiba.opal.elmo.concepts.MissingCategory;
import org.obiba.opal.elmo.concepts.OccurrenceItem;
import org.obiba.opal.elmo.concepts.Opal;
import org.obiba.opal.elmo.concepts.Participant;
import org.obiba.opal.elmo.concepts.hasCategory;
import org.openrdf.elmo.ElmoModule;

/**
 * Builds an ElmoModule with Opal concepts registered.
 */
public class OpalElmoModuleFactory {

  public static ElmoModule createInstance() {
    ElmoModule module = new ElmoModule();
    module.addConcept(Opal.class);

    module.addConcept(DataItem.class);
    module.addConcept(DataVariable.class);
    module.addConcept(Category.class);
    module.addConcept(MissingCategory.class);
    module.addConcept(ContinuousVariable.class);
    module.addConcept(CategoricalVariable.class);
    module.addConcept(ContinuousVariable.class);
    module.addConcept(OccurrenceItem.class);
    module.addConcept(hasCategory.class);

    module.addConcept(Entity.class);
    module.addConcept(Participant.class);
    module.addConcept(Dataset.class);

    return module;

  }

}
