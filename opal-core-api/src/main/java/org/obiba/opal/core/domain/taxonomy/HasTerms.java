package org.obiba.opal.core.domain.taxonomy;

import java.util.List;

import com.google.common.collect.Lists;

public abstract class HasTerms {

  private String name;

  private List<Text> titles;

  private List<Text> descriptions;

  private List<HasTerms> terms;

  public HasTerms() {
  }

  public HasTerms(String name) {
    this.name = name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<Text> getTitles() {
    return titles == null ? titles = Lists.newArrayList() : titles;
  }

  public void setTitles(List<Text> titles) {
    this.titles = titles;
  }

  public void addTitle(Text title) {
    getTitles().add(title);
  }

  public List<Text> getDescriptions() {
    return descriptions == null ? descriptions = Lists.newArrayList() : descriptions;
  }

  public void setDescriptions(List<Text> descriptions) {
    this.descriptions = descriptions;
  }

  public void addDescription(Text description) {
    getDescriptions().add(description);
  }

  public void setTerms(List<HasTerms> terms) {
    this.terms = terms;
  }

  public void add(HasTerms term) {
    getTerms().add(term);
  }

  public boolean hasTerm(String termName) {
    for(HasTerms t : getTerms()) {
      if(t.getName().equals(termName)) {
        return true;
      }
    }
    return false;
  }

  public void removeTerm(String termName) {
    HasTerms term = null;
    for(HasTerms t : getTerms()) {
      if(t.getName().equals(termName)) {
        term = t;
        break;
      }
    }
    if(term != null) {
      getTerms().remove(term);
    }
  }

  public List<HasTerms> getTerms() {
    return terms == null ? terms = Lists.newArrayList() : terms;
  }
}