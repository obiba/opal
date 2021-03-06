/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

ace.define("ace/ext/static_highlight", ["require", "exports", "module", "ace/edit_session", "ace/layer/text"], function (e, t, n) {
    var r = e("../edit_session").EditSession, i = e("../layer/text").Text, s = ".ace_editor {font-family: 'Monaco', 'Menlo', 'Droid Sans Mono', 'Courier New', monospace;font-size: 12px;}.ace_editor .ace_gutter { width: 25px !important;display: block;float: left;text-align: right; padding: 0 3px 0 0; margin-right: 3px;}.ace_line { clear: both; }*.ace_gutter-cell {-moz-user-select: -moz-none;-khtml-user-select: none;-webkit-user-select: none;user-select: none;}";
    t.render = function (e, t, n, o, u) {
        o = parseInt(o || 1, 10);
        var a = new r("");
        a.setMode(t), a.setUseWorker(!1);
        var f = new i(document.createElement("div"));
        f.setSession(a), f.config = {characterWidth: 10, lineHeight: 20}, a.setValue(e);
        var l = [], c = a.getLength();
        for (var h = 0; h < c; h++)l.push("<div class='ace_line'>"), u || l.push("<span class='ace_gutter ace_gutter-cell' unselectable='on'>" + (h + o) + "</span>"), f.$renderLine(l, h, !0, !1), l.push("</div>");
        var p = "<div class=':cssClass'>        <div class='ace_editor ace_scroller ace_text-layer'>            :code        </div>    </div>".replace(/:cssClass/, n.cssClass).replace(/:code/, l.join(""));
        return f.destroy(), {css: s + n.cssText, html: p}
    }
})