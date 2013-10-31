package org.obiba.opal.web.taxonomy;

import java.util.ArrayList;
import java.util.Collection;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.web.model.Opal;

public class Dtos {

  private Dtos() {}

  public static Opal.TaxonomyDto asDto(Taxonomy taxonomy) {
    Opal.TaxonomyDto.Builder builder = Opal.TaxonomyDto.newBuilder();
    builder.setName(taxonomy.getName());
    //TODO
//    builder.addAllTitles(toTextDtoList(taxonomy.getTitles()));
//    builder.addAllDescriptions(toTextDtoList(taxonomy.getDescriptions()));

    Iterable<Opal.VocabularyDto> vocabularyDtos = new ArrayList<Opal.VocabularyDto>();
//    for(Vocabulary v : taxonomy.getVocabularies()) {
//      vocabularyDtos.add(asDto(v));
//    }
    builder.addAllVocabularies(vocabularyDtos);
    return builder.build();
  }

  public static Taxonomy fromDto(Opal.TaxonomyDto dto) {
    Taxonomy taxonomy = new Taxonomy(dto.getName());
    //TODO
//    taxonomy.setTitles(fromTextDtoList(dto.getTitlesList()));
//    taxonomy.setDescriptions(fromTextDtoList(dto.getDescriptionsList()));

    Collection<Vocabulary> vocabularies = new ArrayList<Vocabulary>();
    for(Opal.VocabularyDto v : dto.getVocabulariesList()) {
      vocabularies.add(fromDto(v));
    }
//    taxonomy.setVocabularies(vocabularies);
    return taxonomy;
  }

  public static Opal.TermDto asDto(Term term) {
    Opal.TermDto.Builder builder = Opal.TermDto.newBuilder();
    builder.setName(term.getName());
    //TODO
//    builder.addAllTitles(toTextDtoList(term.getTitles()));
//    builder.addAllDescriptions(toTextDtoList(term.getDescriptions()));
    builder.addAllTerms(asDto(term.getTerms()));
    return builder.build();
  }

  private static Iterable<Opal.TermDto> asDto(Iterable<Term> terms) {
    Iterable<Opal.TermDto> termDto = new ArrayList<Opal.TermDto>();
//    for(HasTerms t : terms) {
//      termDto.add(asDto((Term) t));
//    }
    return termDto;
  }

//  private static List<HasTerms> fromDto(Iterable<Opal.TaxonomyDto.TermDto> termDtos) {
//    List<HasTerms> termDto = new ArrayList<HasTerms>();
//    for(Opal.TaxonomyDto.TermDto t : termDtos) {
//      termDto.add(fromDto(t));
//    }
//    return termDto;
//  }

  public static Term fromDto(Opal.TermDto from) {
    Term term = new Term(from.getName());
    //TODO
//    term.setTitles(fromTextDtoList(from.getTitlesList()));
//    term.setDescriptions(fromTextDtoList(from.getDescriptionsList()));
//    term.setTerms(fromDto(from.getTermsList()));
    return term;
  }

  public static Opal.VocabularyDto asDto(Vocabulary vocabulary) {
    Opal.VocabularyDto.Builder builder = Opal.VocabularyDto.newBuilder();
    builder.setName(vocabulary.getName());
    //TODO
//    builder.addAllTitles(toTextDtoList(vocabulary.getTitles()));
//    builder.addAllDescriptions(toTextDtoList(vocabulary.getDescriptions()));
//    builder.addAllTerms(asDto(vocabulary.getTerms()));
    builder.setRepeatable(vocabulary.isRepeatable());
    return builder.build();
  }

  public static Vocabulary fromDto(Opal.VocabularyDto dto) {
    Vocabulary vocabulary = new Vocabulary();
    //TODO
//    vocabulary.setTitles(fromTextDtoList(dto.getTitlesList()));
//    vocabulary.setDescriptions(fromTextDtoList(dto.getDescriptionsList()));
//    vocabulary.setTerms(fromDto(dto.getTermsList()));
    vocabulary.setRepeatable(dto.getRepeatable());
    return vocabulary;
  }

}
