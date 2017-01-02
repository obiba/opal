/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

define('ace/mode/lisp', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text', 'ace/tokenizer', 'ace/mode/lisp_highlight_rules'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var Tokenizer = require("../tokenizer").Tokenizer;
    var LispHighlightRules = require("./lisp_highlight_rules").LispHighlightRules;

    var Mode = function () {
        var highlighter = new LispHighlightRules();

        this.$tokenizer = new Tokenizer(highlighter.getRules());
    };
    oop.inherits(Mode, TextMode);

    (function () {
    }).call(Mode.prototype);

    exports.Mode = Mode;
});


define('ace/mode/lisp_highlight_rules', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text_highlight_rules'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

    var LispHighlightRules = function () {
        var keywordControl = "case|do|let|loop|if|else|when";
        var keywordOperator = "eq|neq|and|or";
        var constantLanguage = "null|nil";
        var supportFunctions = "cons|car|cdr|cond|lambda|format|setq|setf|quote|eval|append|list|listp|memberp|t|load|progn";

        var keywordMapper = this.createKeywordMapper({
            "keyword.control": keywordControl,
            "keyword.operator": keywordOperator,
            "constant.language": constantLanguage,
            "support.function": supportFunctions
        }, "identifier", true);

        this.$rules =
        {
            "start": [
                {
                    token: "comment",
                    regex: ";.*$"
                },
                {
                    token: ["storage.type.function-type.lisp", "text", "entity.name.function.lisp"],
                    regex: "(?:\\b(?:(defun|defmethod|defmacro))\\b)(\\s+)((?:\\w|\\-|\\!|\\?)*)"
                },
                {
                    token: ["punctuation.definition.constant.character.lisp", "constant.character.lisp"],
                    regex: "(#)((?:\\w|[\\\\+-=<>'\"&#])+)"
                },
                {
                    token: ["punctuation.definition.variable.lisp", "variable.other.global.lisp", "punctuation.definition.variable.lisp"],
                    regex: "(\\*)(\\S*)(\\*)"
                },
                {
                    token: "constant.numeric", // hex
                    regex: "0[xX][0-9a-fA-F]+(?:L|l|UL|ul|u|U|F|f|ll|LL|ull|ULL)?\\b"
                },
                {
                    token: "constant.numeric", // float
                    regex: "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?(?:L|l|UL|ul|u|U|F|f|ll|LL|ull|ULL)?\\b"
                },
                {
                    token: keywordMapper,
                    regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"
                },
                {
                    token: "string",
                    regex: '"(?=.)',
                    next: "qqstring"
                }
            ],
            "qqstring": [
                {
                    token: "constant.character.escape.lisp",
                    regex: "\\\\."
                },
                {
                    token: "string",
                    regex: '[^"\\\\]+'
                },
                {
                    token: "string",
                    regex: "\\\\$",
                    next: "qqstring"
                },
                {
                    token: "string",
                    regex: '"|$',
                    next: "start"
                }
            ]
        }

    };

    oop.inherits(LispHighlightRules, TextHighlightRules);

    exports.LispHighlightRules = LispHighlightRules;
});
