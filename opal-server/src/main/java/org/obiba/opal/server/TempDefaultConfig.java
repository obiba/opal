package org.obiba.opal.server;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.core.service.OrientDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

//TODO to be deleted once UI is fully working
@Component
@Deprecated
@Transactional
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class TempDefaultConfig {

  @Autowired
  private TaxonomyService taxonomyService;

  @Autowired
  private OrientDbService orientDbService;

  public void createDefaultConfig() {

    System.out.println("Creating taxonomies");

    Taxonomy taxonomy1 = new Taxonomy("taxonomy1");
    Vocabulary v = new Vocabulary("body_structures");
    taxonomy1.add(v);
    taxonomyService.saveTaxonomy(taxonomy1);

    taxonomy1 = taxonomyService.getTaxonomy("taxonomy1");
    Vocabulary v2 = new Vocabulary("body_functions");
    taxonomy1.add(v2);
    taxonomyService.saveTaxonomy(taxonomy1);

    printTaxonomies();

    System.out.println("Removing body_structures");

    taxonomy1 = taxonomyService.getTaxonomy("taxonomy1");
    taxonomy1.removeVocabulary("body_structures");

    taxonomyService.saveTaxonomy(taxonomy1);
    printTaxonomies();

//    Taxonomy taxonomy = new Taxonomy("taxonomy2");
//    Vocabulary v3 = new Vocabulary("vocabulary2");
//    taxonomy.add(v3);
//    taxonomyService.saveTaxonomy(taxonomy);
//
//    System.out.println("Bring vocabulary2 to taxonomy1");
//    v3 = taxonomyService.getVocabulary("taxonomy2", "vocabulary2");
//    v3.setTaxonomy(taxonomy1);
//
//    taxonomyService.saveVocabulary("taxonomy2", "vocabulary2", v3);
//
//    printTaxonomies();
  }

  //
  private void printTaxonomies() {
    for(Taxonomy t : taxonomyService.list()) {
      System.out.println(t);
    }
  }

}
