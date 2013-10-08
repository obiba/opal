package org.obiba.opal.web.taxonomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.obiba.opal.core.domain.taxonomy.HasTerms;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Text;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.web.model.Opal;

public class Dtos {

  private Dtos() {}

  public static Opal.TaxonomyDto asDto(Taxonomy taxonomy) {
    Opal.TaxonomyDto.Builder builder = Opal.TaxonomyDto.newBuilder();
    builder.setName(taxonomy.getName());
    builder.addAllTitles(toTextDtoList(taxonomy.getTitles()));
    builder.addAllDescriptions(toTextDtoList(taxonomy.getDescriptions()));

    Collection<Opal.TaxonomyDto.VocabularyDto> vocabularyDtos = new ArrayList<Opal.TaxonomyDto.VocabularyDto>();
    for(Vocabulary v : taxonomy.getVocabularies()) {
      vocabularyDtos.add(asDto(v));
    }
    builder.addAllVocabularies(vocabularyDtos);
    return builder.build();
  }

  public static Taxonomy fromDto(Opal.TaxonomyDto dto) {
    Taxonomy taxonomy = new Taxonomy(dto.getName());
    taxonomy.setTitles(fromTextDtoList(dto.getTitlesList()));
    taxonomy.setDescriptions(fromTextDtoList(dto.getDescriptionsList()));

    List<Vocabulary> vocabularies = new ArrayList<Vocabulary>();
    for(Opal.TaxonomyDto.VocabularyDto v : dto.getVocabulariesList()) {
      vocabularies.add(fromDto(v));
    }
    taxonomy.setVocabularies(vocabularies);
    return taxonomy;
  }

  public static Opal.TaxonomyDto.TextDto asDto(Text text) {
    Opal.TaxonomyDto.TextDto.Builder builder = Opal.TaxonomyDto.TextDto.newBuilder()//
        .setText(text.getText())//
        .setLocale(text.getLocale());
    return builder.build();
  }

  public static Text fromDto(Opal.TaxonomyDto.TextDto from) {
    return new Text(from.getText(), from.getLocale());
  }

  public static Opal.TaxonomyDto.TermDto asDto(Term term) {
    Opal.TaxonomyDto.TermDto.Builder builder = Opal.TaxonomyDto.TermDto.newBuilder();
    builder.setName(term.getName());
    builder.addAllTitles(toTextDtoList(term.getTitles()));
    builder.addAllDescriptions(toTextDtoList(term.getDescriptions()));
    builder.addAllTerms(asDto(term.getTerms()));
    return builder.build();
  }

  private static Iterable<Opal.TaxonomyDto.TextDto> toTextDtoList(Iterable<Text> texts) {
    Collection<Opal.TaxonomyDto.TextDto> textsDto = new ArrayList<Opal.TaxonomyDto.TextDto>();
    for(Text t : texts) {
      textsDto.add(asDto(t));
    }
    return textsDto;
  }

  private static List<Text> fromTextDtoList(Iterable<Opal.TaxonomyDto.TextDto> textDtos) {
    List<Text> texts = new ArrayList<Text>();
    for(Opal.TaxonomyDto.TextDto t : textDtos) {
      texts.add(fromDto(t));
    }
    return texts;
  }

  private static Iterable<Opal.TaxonomyDto.TermDto> asDto(Iterable<HasTerms> terms) {
    Collection<Opal.TaxonomyDto.TermDto> termDto = new ArrayList<Opal.TaxonomyDto.TermDto>();
    for(HasTerms t : terms) {
      termDto.add(asDto((Term) t));
    }
    return termDto;
  }

  private static List<HasTerms> fromDto(Iterable<Opal.TaxonomyDto.TermDto> termDtos) {
    List<HasTerms> termDto = new ArrayList<HasTerms>();
    for(Opal.TaxonomyDto.TermDto t : termDtos) {
      termDto.add(fromDto(t));
    }
    return termDto;
  }

  public static Term fromDto(Opal.TaxonomyDto.TermDto from) {
    Term term = new Term(from.getName());
    term.setTitles(fromTextDtoList(from.getTitlesList()));
    term.setDescriptions(fromTextDtoList(from.getDescriptionsList()));
    term.setTerms(fromDto(from.getTermsList()));
    return term;
  }

  public static Opal.TaxonomyDto.VocabularyDto asDto(Vocabulary vocabulary) {
    Opal.TaxonomyDto.VocabularyDto.Builder builder = Opal.TaxonomyDto.VocabularyDto.newBuilder();
    builder.setName(vocabulary.getName());
    builder.addAllTitles(toTextDtoList(vocabulary.getTitles()));
    builder.addAllDescriptions(toTextDtoList(vocabulary.getDescriptions()));
    builder.addAllTerms(asDto(vocabulary.getTerms()));
    builder.setRepeatable(vocabulary.isRepeatable());
    return builder.build();
  }

  public static Vocabulary fromDto(Opal.TaxonomyDto.VocabularyDto dto) {
    Vocabulary vocabulary = new Vocabulary(dto.getName());
    vocabulary.setTitles(fromTextDtoList(dto.getTitlesList()));
    vocabulary.setDescriptions(fromTextDtoList(dto.getDescriptionsList()));
    vocabulary.setTerms(fromDto(dto.getTermsList()));
    vocabulary.setRepeatable(dto.getRepeatable());
    return vocabulary;
  }

}
