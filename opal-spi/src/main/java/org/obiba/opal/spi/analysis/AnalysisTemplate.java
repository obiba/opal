package org.obiba.opal.spi.analysis;

import org.json.JSONObject;

public interface AnalysisTemplate {

  /**
   * Analysis template unique name.
   *
   * @return
   */
  String getName();

  /**
   * Title of the analysis.
   *
   * @return
   */
  String getTitle();

  /**
   * Markdown can be used to describe the analysis.
   *
   * @return
   */
  String getDescription();

  /**
   * The form to collect analysis parameters, described by a JSON object.
   *
   * @return
   */
  JSONObject getJSONSchemaForm();

}
