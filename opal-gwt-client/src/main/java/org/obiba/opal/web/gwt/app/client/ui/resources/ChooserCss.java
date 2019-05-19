/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui.resources;

import com.watopi.chosen.client.resources.ChozenCss;

public interface ChooserCss extends ChozenCss {

  @ClassName("active-result")
  public String activeResult();

  @ClassName("chzn-choices")
  public String chznChoices();

  @ClassName("chzn-container")
  public String chznContainer();

  @ClassName("chzn-container-active")
  public String chznContainerActive();

  @ClassName("chzn-container-multi")
  public String chznContainerMulti();

  @ClassName("chzn-container-single")
  public String chznContainerSingle();

  @ClassName("chzn-container-single-nosearch")
  public String chznContainerSingleNoSearch();

  @ClassName("chzn-default")
  public String chznDefault();

  @ClassName("chzn-disabled")
  public String chznDisabled();

  @ClassName("chzn-done")
  public String chznDone();

  @ClassName("chzn-drop")
  public String chznDrop();

  @ClassName("chzn-results")
  public String chznResults();

  @ClassName("chzn-results-scroll")
  public String chznResultsScroll();

  @ClassName("chzn-results-scroll-down")
  public String chznResultsScrollDown();

  @ClassName("chzn-results-scroll-up")
  public String chznResultsScrollUp();

  @ClassName("chzn-rtl")
  public String chznRtl();

  @ClassName("chzn-search")
  public String chznSearch();

  @ClassName("chzn-single")
  public String chznSingle();

  @ClassName("chzn-single-with-drop")
  public String chznSingleWithDrop();

  @ClassName("default")
  public String defaultClass();

  @ClassName("group-option")
  public String groupOption();

  @ClassName("group-result")
  public String groupResult();

  @ClassName("highlighted")
  public String highlighted();

  @ClassName("no-results")
  public String noResults();

  @ClassName("result-selected")
  public String resultSelected();

  @ClassName("search-choice")
  public String searchChoice();

  @ClassName("search-choice-close")
  public String searchChoiceClose();

  @ClassName("search-choice-focus")
  public String searchChoiceFocus();

  @ClassName("search-field")
  public String searchField();
}
