/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

ace.define('ace/mode/c9search', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text', 'ace/tokenizer', 'ace/mode/c9search_highlight_rules', 'ace/mode/matching_brace_outdent', 'ace/mode/folding/c9search'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var Tokenizer = require("../tokenizer").Tokenizer;
    var C9SearchHighlightRules = require("./c9search_highlight_rules").C9SearchHighlightRules;
    var MatchingBraceOutdent = require("./matching_brace_outdent").MatchingBraceOutdent;
    var C9StyleFoldMode = require("./folding/c9search").FoldMode;

    var Mode = function () {
        this.$tokenizer = new Tokenizer(new C9SearchHighlightRules().getRules(), "i");
        this.$outdent = new MatchingBraceOutdent();
        this.foldingRules = new C9StyleFoldMode();
    };
    oop.inherits(Mode, TextMode);

    (function () {

        this.getNextLineIndent = function (state, line, tab) {
            var indent = this.$getIndent(line);
            return indent;
        };

        this.checkOutdent = function (state, line, input) {
            return this.$outdent.checkOutdent(line, input);
        };

        this.autoOutdent = function (state, doc, row) {
            this.$outdent.autoOutdent(doc, row);
        };

    }).call(Mode.prototype);

    exports.Mode = Mode;

});

ace.define('ace/mode/c9search_highlight_rules', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text_highlight_rules'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

    var C9SearchHighlightRules = function () {
        this.$rules = {
            "start": [
                {
                    token: ["c9searchresults.constant.numeric", "c9searchresults.text", "c9searchresults.text"],
                    regex: "(^\\s+[0-9]+)(:\\s*)(.+)"
                },
                {
                    token: ["string", "text"], // single line
                    regex: "(.+)(:$)"
                }
            ]
        };
    };

    oop.inherits(C9SearchHighlightRules, TextHighlightRules);

    exports.C9SearchHighlightRules = C9SearchHighlightRules;

});

ace.define('ace/mode/matching_brace_outdent', ['require', 'exports', 'module' , 'ace/range'], function (require, exports, module) {


    var Range = require("../range").Range;

    var MatchingBraceOutdent = function () {
    };

    (function () {

        this.checkOutdent = function (line, input) {
            if (!/^\s+$/.test(line))
                return false;

            return /^\s*\}/.test(input);
        };

        this.autoOutdent = function (doc, row) {
            var line = doc.getLine(row);
            var match = line.match(/^(\s*\})/);

            if (!match) return 0;

            var column = match[1].length;
            var openBracePos = doc.findMatchingBracket({row: row, column: column});

            if (!openBracePos || openBracePos.row == row) return 0;

            var indent = this.$getIndent(doc.getLine(openBracePos.row));
            doc.replace(new Range(row, 0, row, column - 1), indent);
        };

        this.$getIndent = function (line) {
            var match = line.match(/^(\s+)/);
            if (match) {
                return match[1];
            }

            return "";
        };

    }).call(MatchingBraceOutdent.prototype);

    exports.MatchingBraceOutdent = MatchingBraceOutdent;
});


ace.define('ace/mode/folding/c9search', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/range', 'ace/mode/folding/fold_mode'], function (require, exports, module) {


    var oop = require("../../lib/oop");
    var Range = require("../../range").Range;
    var BaseFoldMode = require("./fold_mode").FoldMode;

    var FoldMode = exports.FoldMode = function () {
    };
    oop.inherits(FoldMode, BaseFoldMode);

    (function () {

        this.foldingStartMarker = /^(\S.*\:|Searching for.*)$/;
        this.foldingStopMarker = /^(\s+|Found.*)$/;

        this.getFoldWidgetRange = function (session, foldStyle, row) {
            var lines = session.doc.getAllLines(row);
            var line = lines[row];
            var level1 = /^(Found.*|Searching for.*)$/;
            var level2 = /^(\S.*\:|\s*)$/;
            var re = level1.test(line) ? level1 : level2;

            if (this.foldingStartMarker.test(line)) {
                for (var i = row + 1, l = session.getLength(); i < l; i++) {
                    if (re.test(lines[i]))
                        break;
                }

                return new Range(row, line.length, i, 0);
            }

            if (this.foldingStopMarker.test(line)) {
                for (var i = row - 1; i >= 0; i--) {
                    line = lines[i];
                    if (re.test(line))
                        break;
                }

                return new Range(i, line.length, row, 0);
            }
        };

    }).call(FoldMode.prototype);

});

