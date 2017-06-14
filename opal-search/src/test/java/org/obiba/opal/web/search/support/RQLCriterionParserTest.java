/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.search.support;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.opal.core.domain.VariableNature;

import static org.fest.assertions.api.Assertions.assertThat;

public class RQLCriterionParserTest {
  
  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
  }

  @After
  public void tearDown() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_in_single() {
    String rql = "in(field,1)";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:(1)");
  }

  @Test
  public void test_in_single_categorical() {
    String rql = "in(field,1)";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected VariableNature getNature() {
        return VariableNature.CATEGORICAL;
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:(\"1\")");
  }


  @Test
  public void test_in_single_categorical_wildcard() {
    String rql = "in(field,*)";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected VariableNature getNature() {
        return VariableNature.CATEGORICAL;
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:(*)");
  }

  @Test
  public void test_in_single_spaced() {
    String rql = "in(field,1 2)";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:(1+2)");
  }

  @Test
  public void test_in_single_wildcard() {
    String rql = "in(field,*)";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:(*)");
  }

  @Test
  public void test_not_in_single() {
    String rql = "not(in(field,1))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("NOT field:(1)");
  }

  @Test
  public void test_in_singleton() {
    String rql = "in(field,(1))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:(1)");
  }

  @Test
  public void test_in_multiple() {
    String rql = "in(field,(1,2))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:(1 OR 2)");
  }

  @Test
  public void test_in_multiple_categorical() {
    String rql = "in(field,(1,2))";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected VariableNature getNature() {
        return VariableNature.CATEGORICAL;
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:(\"1\" OR \"2\")");
  }

  @Test
  public void test_in_multiple_categorical_wildcard() {
    String rql = "in(field,(1*,2))";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected VariableNature getNature() {
        return VariableNature.CATEGORICAL;
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:(1* OR \"2\")");
  }

  @Test
  public void test_in_multiple_spaced() {
    String rql = "in(field,(1 2))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:(1+2)");
  }

  @Test
  public void test_in_multiple_or() {
    String rql = "in(field,or(1,2))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:(1 OR 2)");
  }

  @Test
  public void test_in_multiple_and() {
    String rql = "in(field,and(1,2))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:(1 AND 2)");
  }

  @Test
  public void test_exists() {
    String rql = "exists(field)";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("_exists_:field");
  }

  @Test
  public void test_not_exists() {
    String rql = "not(exists(field))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("NOT _exists_:field");
  }

  @Test
  public void test_range() {
    String rql = "range(field,(1,10))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:[1 TO 10]");
  }

  @Test
  public void test_range_wildcard_right() {
    String rql = "range(field,(1,*))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:[1 TO *]");
  }

  @Test
  public void test_range_wildcard_left() {
    String rql = "range(field,(*,10))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:[* TO 10]");
  }
  @Test
  public void test_range_wildcards() {
    String rql = "range(field,(*,*))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("field:[* TO *]");
  }

  @Test
  public void test_range_date() {
    String rql = "range(field,(2017-06-01,2017-06-15))";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected ValueType getValueType() {
        return DateType.get();
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:[2017-06-01 TO 2017-06-15]");
  }

  @Test
  public void test_range_date_wildcard_right() {
    String rql = "range(field,(2017-06-01,*))";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected ValueType getValueType() {
        return DateType.get();
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:[2017-06-01 TO *]");
  }

  @Test
  public void test_range_date_wildcard_left() {
    String rql = "range(field,(*,2017-06-01))";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected ValueType getValueType() {
        return DateType.get();
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:[* TO 2017-06-01]");
  }

  @Test
  public void test_range_date_wildcards() {
    String rql = "range(field,(*,*))";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected ValueType getValueType() {
        return DateType.get();
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:[* TO *]");
  }

  @Test
  public void test_range_datetime() {
    String rql = "range(field,(2017-06-01,2017-06-15))";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected ValueType getValueType() {
        return DateTimeType.get();
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:[2017-06-01 TO 2017-06-15]");
  }

  @Test
  public void test_in_singleton_date() {
    String rql = "in(field,(2017-06-01))";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected ValueType getValueType() {
        return DateType.get();
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:(>=2017-06-01 AND <=2017-06-01)");
  }

  @Test
  public void test_in_singleton_date_wildcard() {
    String rql = "in(field,(*))";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected ValueType getValueType() {
        return DateType.get();
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:(*)");
  }

  @Test
  public void test_in_single_date() {
    String rql = "in(field,2017-06-01)";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected ValueType getValueType() {
        return DateType.get();
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:(>=2017-06-01 AND <=2017-06-01)");
  }

  @Test
  public void test_in_single_date_wildcard() {
    String rql = "in(field,*)";
    RQLCriterionParser parser = new RQLCriterionParser(rql) {
      @Override
      protected ValueType getValueType() {
        return DateType.get();
      }
    };
    assertThat(parser.getQuery()).isEqualTo("field:(*)");
  }

  @Test
  public void test_like_single() {
    String rql = "like(field,1)";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("(field:(1) OR field.analyzed:(1))");
  }

  @Test
  public void test_like_multiple() {
    String rql = "like(field,(1,2*))";
    RQLCriterionParser parser = new RQLCriterionParser(rql);
    assertThat(parser.getQuery()).isEqualTo("(field:(1 OR 2*) OR field.analyzed:(1 OR 2*))");
  }
}
