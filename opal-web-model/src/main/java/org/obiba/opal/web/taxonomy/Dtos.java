package org.obiba.opal.web.taxonomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.web.model.Opal;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class Dtos {

  private Dtos() {}

  public static Opal.TaxonomyDto asDto(Taxonomy taxonomy) {
    Opal.TaxonomyDto.Builder builder = Opal.TaxonomyDto.newBuilder();
    builder.setName(taxonomy.getName());
    builder.addAllTitle(toLocaleTextDtoList(taxonomy.getTitle()));
    builder.addAllDescription(toLocaleTextDtoList(taxonomy.getDescription()));

    if(taxonomy.hasVocabularies()) {
      builder.addAllVocabularies(
          Iterables.transform(taxonomy.getVocabularies(), new Function<Vocabulary, Opal.VocabularyDto>() {
            @Nullable
            @Override
            public Opal.VocabularyDto apply(@Nullable Vocabulary input) {
              return asDto(input);
            }
          }));
    }
    return builder.build();
  }

  public static Opal.TaxonomiesDto.TaxonomySummaryDto asSummaryDto(Taxonomy taxonomy) {
    Opal.TaxonomiesDto.TaxonomySummaryDto.Builder builder = Opal.TaxonomiesDto.TaxonomySummaryDto.newBuilder();
    builder.setName(taxonomy.getName());
    builder.addAllTitle(toLocaleTextDtoList(taxonomy.getTitle()));
    return builder.build();
  }

  private static Iterable<? extends Opal.LocaleTextDto> toLocaleTextDtoList(Map<String, String> map) {
    Collection<Opal.LocaleTextDto> localeTexts = new ArrayList<>();

    for(String locale : map.keySet()) {
      localeTexts.add(
          Opal.LocaleTextDto.newBuilder().setText(map.get(locale)).setLocale(new Locale(locale).toLanguageTag())
              .build());
    }

    return localeTexts;
  }

  public static Taxonomy fromDto(Opal.TaxonomyDto dto) {
    Taxonomy taxonomy = new Taxonomy(dto.getName());
    taxonomy.setTitle(fromLocaleTextDtoList(dto.getTitleList()));
    taxonomy.setDescription(fromLocaleTextDtoList(dto.getDescriptionList()));

    for(Opal.VocabularyDto vocabulary : dto.getVocabulariesList()) {
      taxonomy.addVocabulary(fromDto(vocabulary));
    }

    return taxonomy;
  }

  private static Opal.TermDto asDto(Term term) {
    Opal.TermDto.Builder builder = Opal.TermDto.newBuilder();
    builder.setName(term.getName());
    builder.addAllTitle(toLocaleTextDtoList(term.getTitle()));
    builder.addAllDescription(toLocaleTextDtoList(term.getDescription()));
    return builder.build();
  }

  private static Iterable<Opal.TermDto> asDto(Iterable<Term> terms) {
    Collection<Opal.TermDto> termDto = new ArrayList<>();
    for(Term t : terms) {
      termDto.add(asDto(t));
    }
    return termDto;
  }

  private static List<Term> fromDto(Iterable<Opal.TermDto> termDtos) {
    List<Term> termDto = new ArrayList<>();
    for(Opal.TermDto t : termDtos) {
      termDto.add(fromDto(t));
    }
    return termDto;
  }

  public static Term fromDto(Opal.TermDto from) {
    Term term = new Term(from.getName());
    term.setTitle(fromLocaleTextDtoList(from.getTitleList()));
    term.setDescription(fromLocaleTextDtoList(from.getDescriptionList()));
    return term;
  }

  public static Opal.VocabularyDto asDto(Vocabulary vocabulary) {
    Opal.VocabularyDto.Builder builder = Opal.VocabularyDto.newBuilder();
    builder.setName(vocabulary.getName());
    builder.addAllTitle(toLocaleTextDtoList(vocabulary.getTitle()));
    builder.addAllDescription(toLocaleTextDtoList(vocabulary.getDescription()));
    if(vocabulary.hasTerms()) builder.addAllTerms(asDto(vocabulary.getTerms()));
    builder.setRepeatable(vocabulary.isRepeatable());
    return builder.build();
  }

  public static Vocabulary fromDto(Opal.VocabularyDto dto) {
    Vocabulary vocabulary = new Vocabulary(dto.getName());
    vocabulary.setTitle(fromLocaleTextDtoList(dto.getTitleList()));
    vocabulary.setDescription(fromLocaleTextDtoList(dto.getDescriptionList()));
    vocabulary.setRepeatable(dto.getRepeatable());
    vocabulary.setTerms(fromDto(dto.getTermsList()));
    return vocabulary;
  }

  private static Map<String, String> fromLocaleTextDtoList(Iterable<Opal.LocaleTextDto> dtos) {
    Map<String, String> localeTexts = new HashMap<>();

    for(Opal.LocaleTextDto dto : dtos) {
      localeTexts.put(new Locale(dto.getLocale()).toLanguageTag(), dto.getText());
    }

    return localeTexts;
  }
}
