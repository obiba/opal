/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

ace.define('ace/mode/textile', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text', 'ace/tokenizer', 'ace/mode/textile_highlight_rules', 'ace/mode/matching_brace_outdent'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var Tokenizer = require("../tokenizer").Tokenizer;
    var TextileHighlightRules = require("./textile_highlight_rules").TextileHighlightRules;
    var MatchingBraceOutdent = require("./matching_brace_outdent").MatchingBraceOutdent;

    var Mode = function () {
        this.$tokenizer = new Tokenizer(new TextileHighlightRules().getRules());
        this.$outdent = new MatchingBraceOutdent();
    };
    oop.inherits(Mode, TextMode);

    (function () {
        this.getNextLineIndent = function (state, line, tab) {
            if (state == "intag")
                return tab;

            return "";
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

ace.define('ace/mode/textile_highlight_rules', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text_highlight_rules'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

    var TextileHighlightRules = function () {
        this.$rules = {
            "start": [
                {
                    token: function (value) {
                        if (value.charAt(0) == "h")
                            return "markup.heading." + value.charAt(1);
                        else
                            return "markup.heading";
                    },
                    regex: "h1|h2|h3|h4|h5|h6|bq|p|bc|pre",
                    next: "blocktag"
                },
                {
                    token: "keyword",
                    regex: "[\\*]+|[#]+"
                },
                {
                    token: "text",
                    regex: ".+"
                }
            ],
            "blocktag": [
                {
                    token: "keyword",
                    regex: "\\. ",
                    next: "start"
                },
                {
                    token: "keyword",
                    regex: "\\(",
                    next: "blocktagproperties"
                }
            ],
            "blocktagproperties": [
                {
                    token: "keyword",
                    regex: "\\)",
                    next: "blocktag"
                },
                {
                    token: "string",
                    regex: "[a-zA-Z0-9\\-_]+"
                },
                {
                    token: "keyword",
                    regex: "#"
                }
            ]
        };
    };

    oop.inherits(TextileHighlightRules, TextHighlightRules);

    exports.TextileHighlightRules = TextileHighlightRules;

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
