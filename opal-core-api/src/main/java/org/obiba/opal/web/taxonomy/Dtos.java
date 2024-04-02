/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.taxonomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.annotation.Nullable;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.web.model.Opal;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class Dtos {

  private Dtos() {}

  public static Opal.TaxonomyDto asDto(Taxonomy taxonomy) {
    return asDto(taxonomy, true);
  }

  public static Opal.TaxonomyDto asDto(Taxonomy taxonomy, boolean withVocabularies) {
    Opal.TaxonomyDto.Builder builder = Opal.TaxonomyDto.newBuilder();
    builder.setName(taxonomy.getName());
    if(taxonomy.hasAuthor()) builder.setAuthor(taxonomy.getAuthor());
    if(taxonomy.hasLicense()) builder.setLicense(taxonomy.getLicense());
    builder.addAllTitle(toLocaleTextDtoList(taxonomy.getTitle()));
    builder.addAllDescription(toLocaleTextDtoList(taxonomy.getDescription()));
    builder.addAllKeywords(toLocaleTextDtoList(taxonomy.getKeywords()));
    builder.addAllAttributes(toEntryDtoList(taxonomy.getAttributes()));

    if(withVocabularies && taxonomy.hasVocabularies()) {
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

  public static Opal.TaxonomiesDto.TaxonomySummaryDto asVocabularySummaryDto(Taxonomy taxonomy) {
    Opal.TaxonomiesDto.TaxonomySummaryDto.Builder builder = Opal.TaxonomiesDto.TaxonomySummaryDto.newBuilder();
    builder.setName(taxonomy.getName());
    builder.addAllTitle(toLocaleTextDtoList(taxonomy.getTitle()));

    if(taxonomy.hasVocabularies()) {
      builder.addAllVocabularySummaries(Iterables.transform(taxonomy.getVocabularies(),
          new Function<Vocabulary, Opal.TaxonomiesDto.TaxonomySummaryDto.VocabularySummaryDto>() {
            @Nullable
            @Override
            public Opal.TaxonomiesDto.TaxonomySummaryDto.VocabularySummaryDto apply(@Nullable Vocabulary input) {
              return asSummaryDto(input);
            }
          }));
    }

    return builder.build();
  }

  public static Opal.TaxonomiesDto.TaxonomySummaryDto.VocabularySummaryDto asSummaryDto(Vocabulary vocabulary) {
    Opal.TaxonomiesDto.TaxonomySummaryDto.VocabularySummaryDto.Builder builder
        = Opal.TaxonomiesDto.TaxonomySummaryDto.VocabularySummaryDto.newBuilder();
    builder.setName(vocabulary.getName());
    builder.addAllTitle(toLocaleTextDtoList(vocabulary.getTitle()));
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

  private static Iterable<? extends Opal.EntryDto> toEntryDtoList(Map<String, String> map) {
    Collection<Opal.EntryDto> entries = new ArrayList<>();

    for(String key : map.keySet()) {
      entries.add(Opal.EntryDto.newBuilder().setKey(key).setValue(map.get(key)).build());
    }

    return entries;
  }

  public static Taxonomy fromDto(Opal.TaxonomyDto dto) {
    Taxonomy taxonomy = new Taxonomy(dto.getName());
    if(dto.hasAuthor()) taxonomy.setAuthor(dto.getAuthor());
    if(dto.hasLicense()) taxonomy.setLicense(dto.getLicense());
    taxonomy.setTitle(fromLocaleTextDtoList(dto.getTitleList()));
    taxonomy.setDescription(fromLocaleTextDtoList(dto.getDescriptionList()));
    taxonomy.setKeywords(fromLocaleTextDtoList(dto.getKeywordsList()));
    taxonomy.setAttributes(fromEntryDtoList(dto.getAttributesList()));

    for(Opal.VocabularyDto vocabulary : dto.getVocabulariesList()) {
      if(vocabulary.hasName()) {
        taxonomy.addVocabulary(fromDto(vocabulary));
      }
    }

    return taxonomy;
  }

  public static Opal.TermDto asDto(Term term) {
    Opal.TermDto.Builder builder = Opal.TermDto.newBuilder();
    builder.setName(term.getName());
    builder.addAllTitle(toLocaleTextDtoList(term.getTitle()));
    builder.addAllDescription(toLocaleTextDtoList(term.getDescription()));
    builder.addAllKeywords(toLocaleTextDtoList(term.getKeywords()));
    builder.addAllAttributes(toEntryDtoList(term.getAttributes()));
    if(term.hasTerms()) builder.addAllTerms(asDto(term.getTerms()));
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
      if(t.hasName()) {
        termDto.add(fromDto(t));
      }
    }
    return termDto;
  }

  public static Term fromDto(Opal.TermDto from) {
    Term term = new Term(from.getName());
    term.setTitle(fromLocaleTextDtoList(from.getTitleList()));
    term.setDescription(fromLocaleTextDtoList(from.getDescriptionList()));
    term.setKeywords(fromLocaleTextDtoList(from.getKeywordsList()));
    term.setAttributes(fromEntryDtoList(from.getAttributesList()));
    term.setTerms(fromDto(from.getTermsList()));
    return term;
  }

  public static Opal.VocabularyDto asDto(Vocabulary vocabulary) {
    return asDto(vocabulary, true);
  }

  public static Opal.VocabularyDto asDto(Vocabulary vocabulary, boolean withTerms) {
    Opal.VocabularyDto.Builder builder = Opal.VocabularyDto.newBuilder();
    builder.setName(vocabulary.getName());
    builder.addAllTitle(toLocaleTextDtoList(vocabulary.getTitle()));
    builder.addAllDescription(toLocaleTextDtoList(vocabulary.getDescription()));
    builder.addAllKeywords(toLocaleTextDtoList(vocabulary.getKeywords()));
    builder.addAllAttributes(toEntryDtoList(vocabulary.getAttributes()));
    if(withTerms && vocabulary.hasTerms()) builder.addAllTerms(asDto(vocabulary.getTerms()));
    builder.setRepeatable(vocabulary.isRepeatable());
    return builder.build();
  }

  public static Vocabulary fromDto(Opal.VocabularyDto dto) {
    Vocabulary vocabulary = new Vocabulary(dto.getName());
    vocabulary.setTitle(fromLocaleTextDtoList(dto.getTitleList()));
    vocabulary.setDescription(fromLocaleTextDtoList(dto.getDescriptionList()));
    vocabulary.setKeywords(fromLocaleTextDtoList(dto.getKeywordsList()));
    vocabulary.setAttributes(fromEntryDtoList(dto.getAttributesList()));
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

  private static Map<String, String> fromEntryDtoList(Iterable<Opal.EntryDto> dtos) {
    Map<String, String> entries = new HashMap<>();

    for(Opal.EntryDto dto : dtos) {
      entries.put(dto.getKey(), dto.getValue());
    }

    return entries;
  }
}
