/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

define('ace/ext/static_highlight', ['require', 'exports', 'module' , 'ace/edit_session', 'ace/layer/text'], function (require, exports, module) {


    var EditSession = require("../edit_session").EditSession;
    var TextLayer = require("../layer/text").Text;
    var baseStyles = ".ace_editor {\
font-family: 'Monaco', 'Menlo', 'Droid Sans Mono', 'Courier New', monospace;\
font-size: 12px;\
}\
.ace_editor .ace_gutter { \
width: 25px !important;\
display: block;\
float: left;\
text-align: right; \
padding: 0 3px 0 0; \
margin-right: 3px;\
}\
.ace_line { clear: both; }\
*.ace_gutter-cell {\
-moz-user-select: -moz-none;\
-khtml-user-select: none;\
-webkit-user-select: none;\
user-select: none;\
}";

    exports.render = function (input, mode, theme, lineStart, disableGutter) {
        lineStart = parseInt(lineStart || 1, 10);

        var session = new EditSession("");
        session.setMode(mode);
        session.setUseWorker(false);

        var textLayer = new TextLayer(document.createElement("div"));
        textLayer.setSession(session);
        textLayer.config = {
            characterWidth: 10,
            lineHeight: 20
        };

        session.setValue(input);

        var stringBuilder = [];
        var length = session.getLength();

        for (var ix = 0; ix < length; ix++) {
            stringBuilder.push("<div class='ace_line'>");
            if (!disableGutter)
                stringBuilder.push("<span class='ace_gutter ace_gutter-cell' unselectable='on'>" + (ix + lineStart) + "</span>");
            textLayer.$renderLine(stringBuilder, ix, true, false);
            stringBuilder.push("</div>");
        }
        var html = "<div class=':cssClass'>\
        <div class='ace_editor ace_scroller ace_text-layer'>\
            :code\
        </div>\
    </div>".replace(/:cssClass/, theme.cssClass).replace(/:code/, stringBuilder.join(""));

        textLayer.destroy();

        return {
            css: baseStyles + theme.cssText,
            html: html
        };
    };

});
