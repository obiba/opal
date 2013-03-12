/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;

import org.junit.Test;

public class QueryTermJsonBuilderTest {

  @Test
  public void testBuild_QueryTerm() throws Exception {

    QueryTermJsonBuilder.QueryTermsFiltersBuilder filtersBuilder = new QueryTermJsonBuilder.QueryTermsFiltersBuilder();
//    filtersBuilder.addFilterValue("opal-data.bloodsamplescollection");
//    filtersBuilder.addFilterValue("opal-data.bloodpressure");
//    filtersBuilder.addFilterValue("opal-data.anklebrachial");
//    filtersBuilder.addFilterValue("opal-data.hop");

    filtersBuilder.addFilterValue("opal-data.anklebrachial");
    filtersBuilder.addFilterValue("opal-data.armspan");
    filtersBuilder.addFilterValue("opal-data.bloodpressure");
    filtersBuilder.addFilterValue("opal-data.bloodsamplescollection");
    filtersBuilder.addFilterValue("opal-data.bonedensity");
    filtersBuilder.addFilterValue("opal-data.cipreliminaryquestionnaire");
    filtersBuilder.addFilterValue("opal-data.conclusionquestionnaire");
    filtersBuilder.addFilterValue("opal-data.consent");
    filtersBuilder.addFilterValue("opal-data.gripstrength");
    filtersBuilder.addFilterValue("opal-data.healthquestionnairenurse");
    filtersBuilder.addFilterValue("opal-data.healthquestionnairetouchscreen");
    filtersBuilder.addFilterValue("opal-data.hips");
    filtersBuilder.addFilterValue("opal-data.impedance418");
    filtersBuilder.addFilterValue("opal-data.hop");
    filtersBuilder.addFilterValue("opal-data.salivasamplescollection");
    filtersBuilder.addFilterValue("opal-data.sittingheight");
    filtersBuilder.addFilterValue("opal-data.spirometry");
    filtersBuilder.addFilterValue("opal-data.standingheight");
    filtersBuilder.addFilterValue("opal-data.urinesamplescollection");
    filtersBuilder.addFilterValue("opal-data.waist");
    filtersBuilder.addFilterValue("opal-data.weight");
    filtersBuilder.setFieldName("_type");

    QueryTermJsonBuilder queryBuilder = new QueryTermJsonBuilder().setTermFieldName("_id").setTermFieldValue("6436175")
        .setTermFilters(filtersBuilder.build());

    System.out.println(">>>>> " + queryBuilder.build().toString());

  }

}