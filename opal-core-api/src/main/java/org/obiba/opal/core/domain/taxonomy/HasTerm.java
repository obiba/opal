package org.obiba.opal.core.domain.taxonomy;

import java.util.List;

import com.google.common.collect.Lists;

public abstract class HasTerm {

  private String name;

  private List<Text> titles;

  private List<Text> descriptions;

  private List<HasTerm> terms;

  HasTerm() {
  }

  HasTerm(String name) {
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

  public void setTerms(List<HasTerm> terms) {
    this.terms = terms;
  }

  public void add(HasTerm term) {
    getTerms().add(term);
  }

  public boolean hasTerm(String termName) {
    for(HasTerm t : getTerms()) {
      if(t.getName().equals(termName)) {
        return true;
      }
    }
    return false;
  }

  public void removeTerm(String termName) {
    HasTerm term = null;
    for(HasTerm t : getTerms()) {
      if(t.getName().equals(termName)) {
        term = t;
        break;
      }
    }
    if(term != null) {
      getTerms().remove(term);
    }
  }

  public List<HasTerm> getTerms() {
    return terms == null ? terms = Lists.newArrayList() : terms;
  }
}