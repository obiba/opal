package org.obiba.opal.web.taxonomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.web.model.Opal;

public class Dtos {

  private Dtos() {}

  public static Opal.TaxonomyDto asDto(Taxonomy taxonomy) {
    Opal.TaxonomyDto.Builder builder = Opal.TaxonomyDto.newBuilder();
    builder.setName(taxonomy.getName());
    builder.addAllTitles(toLocaleTextDtoList(taxonomy.getTitles()));
    builder.addAllDescriptions(toLocaleTextDtoList(taxonomy.getDescriptions()));

    if(taxonomy.getVocabularies() != null) {
      builder.addAllVocabularies(taxonomy.getVocabularies());
    }
    return builder.build();
  }

  private static Iterable<? extends Opal.LocaleTextDto> toLocaleTextDtoList(Map<Locale, String> map) {
    Collection<Opal.LocaleTextDto> localeTexts = new ArrayList<Opal.LocaleTextDto>();

    for(Locale locale : map.keySet()) {
      localeTexts.add(Opal.LocaleTextDto.newBuilder().setText(map.get(locale)).setLocale(locale.getLanguage()).build());
    }

    return localeTexts;
  }

  public static Taxonomy fromDto(Opal.TaxonomyDto dto) {
    Taxonomy taxonomy = new Taxonomy(dto.getName());
    taxonomy.setTitles(fromLocaleTextDtoList(dto.getTitlesList()));
    taxonomy.setDescriptions(fromLocaleTextDtoList(dto.getDescriptionsList()));

    for(String vocabulary : dto.getVocabulariesList()) {
      taxonomy.addVocabulary(vocabulary);
    }

    return taxonomy;
  }

  private static Opal.TermDto asDto(Term term) {
    Opal.TermDto.Builder builder = Opal.TermDto.newBuilder();
    builder.setName(term.getName());
    builder.addAllTitles(toLocaleTextDtoList(term.getTitles()));
    builder.addAllDescriptions(toLocaleTextDtoList(term.getDescriptions()));
    builder.addAllTerms(asDto(term.getTerms()));
    return builder.build();
  }

  private static Iterable<Opal.TermDto> asDto(Iterable<Term> terms) {
    Collection<Opal.TermDto> termDto = new ArrayList<Opal.TermDto>();
    for(Term t : terms) {
      termDto.add(asDto(t));
    }
    return termDto;
  }

  private static List<Term> fromDto(Iterable<Opal.TermDto> termDtos) {
    List<Term> termDto = new ArrayList<Term>();
    for(Opal.TermDto t : termDtos) {
      termDto.add(fromDto(t));
    }
    return termDto;
  }

  private static Term fromDto(Opal.TermDto from) {
    Term term = new Term(from.getName());
    term.setTitles(fromLocaleTextDtoList(from.getTitlesList()));
    term.setDescriptions(fromLocaleTextDtoList(from.getDescriptionsList()));
    term.setTerms(fromDto(from.getTermsList()));
    return term;
  }

  public static Opal.VocabularyDto asDto(Vocabulary vocabulary) {
    Opal.VocabularyDto.Builder builder = Opal.VocabularyDto.newBuilder();
    builder.setName(vocabulary.getName());
    builder.addAllTitles(toLocaleTextDtoList(vocabulary.getTitles()));
    builder.addAllDescriptions(toLocaleTextDtoList(vocabulary.getDescriptions()));
    builder.addAllTerms(asDto(vocabulary.getTerms()));
    builder.setRepeatable(vocabulary.isRepeatable());
    builder.setTaxonomyName(vocabulary.getTaxonomy());
    return builder.build();
  }

  public static Vocabulary fromDto(String taxonomyName, Opal.VocabularyDto dto) {
    Vocabulary vocabulary = new Vocabulary(taxonomyName, dto.getName());
    vocabulary.setTitles(fromLocaleTextDtoList(dto.getTitlesList()));
    vocabulary.setDescriptions(fromLocaleTextDtoList(dto.getDescriptionsList()));
    vocabulary.setTerms(fromDto(dto.getTermsList()));
    vocabulary.setRepeatable(dto.getRepeatable());
    return vocabulary;
  }

  private static Map<Locale, String> fromLocaleTextDtoList(Iterable<Opal.LocaleTextDto> dtos) {
    Map<Locale, String> localeTexts = new HashMap<Locale, String>();

    for(Opal.LocaleTextDto dto : dtos) {
      localeTexts.put(new Locale(dto.getLocale()), dto.getText());
    }

    return localeTexts;
  }
}
