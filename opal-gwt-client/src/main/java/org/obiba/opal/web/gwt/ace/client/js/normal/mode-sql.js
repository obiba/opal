/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

define('ace/mode/sql', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text', 'ace/tokenizer', 'ace/mode/sql_highlight_rules', 'ace/range'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var Tokenizer = require("../tokenizer").Tokenizer;
    var SqlHighlightRules = require("./sql_highlight_rules").SqlHighlightRules;
    var Range = require("../range").Range;

    var Mode = function () {
        this.$tokenizer = new Tokenizer(new SqlHighlightRules().getRules());
    };
    oop.inherits(Mode, TextMode);

    (function () {

        this.toggleCommentLines = function (state, doc, startRow, endRow) {
            var outdent = true;
            var outentedRows = [];
            var re = /^(\s*)--/;

            for (var i = startRow; i <= endRow; i++) {
                if (!re.test(doc.getLine(i))) {
                    outdent = false;
                    break;
                }
            }

            if (outdent) {
                var deleteRange = new Range(0, 0, 0, 0);
                for (var i = startRow; i <= endRow; i++) {
                    var line = doc.getLine(i);
                    var m = line.match(re);
                    deleteRange.start.row = i;
                    deleteRange.end.row = i;
                    deleteRange.end.column = m[0].length;
                    doc.replace(deleteRange, m[1]);
                }
            }
            else {
                doc.indentRows(startRow, endRow, "--");
            }
        };

    }).call(Mode.prototype);

    exports.Mode = Mode;

});

define('ace/mode/sql_highlight_rules', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text_highlight_rules'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

    var SqlHighlightRules = function () {

        var keywords = (
            "select|insert|update|delete|from|where|and|or|group|by|order|limit|offset|having|as|case|" +
                "when|else|end|type|left|right|join|on|outer|desc|asc"
            );

        var builtinConstants = (
            "true|false|null"
            );

        var builtinFunctions = (
            "count|min|max|avg|sum|rank|now|coalesce"
            );

        var keywordMapper = this.createKeywordMapper({
            "support.function": builtinFunctions,
            "keyword": keywords,
            "constant.language": builtinConstants
        }, "identifier", true);

        this.$rules = {
            "start": [
                {
                    token: "comment",
                    regex: "--.*$"
                },
                {
                    token: "string",           // " string
                    regex: '".*?"'
                },
                {
                    token: "string",           // ' string
                    regex: "'.*?'"
                },
                {
                    token: "constant.numeric", // float
                    regex: "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"
                },
                {
                    token: keywordMapper,
                    regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"
                },
                {
                    token: "keyword.operator",
                    regex: "\\+|\\-|\\/|\\/\\/|%|<@>|@>|<@|&|\\^|~|<|>|<=|=>|==|!=|<>|="
                },
                {
                    token: "paren.lparen",
                    regex: "[\\(]"
                },
                {
                    token: "paren.rparen",
                    regex: "[\\)]"
                },
                {
                    token: "text",
                    regex: "\\s+"
                }
            ]
        };
    };

    oop.inherits(SqlHighlightRules, TextHighlightRules);

    exports.SqlHighlightRules = SqlHighlightRules;
});

