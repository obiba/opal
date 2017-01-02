/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

define('ace/mode/sh', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text', 'ace/tokenizer', 'ace/mode/sh_highlight_rules', 'ace/range'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var Tokenizer = require("../tokenizer").Tokenizer;
    var ShHighlightRules = require("./sh_highlight_rules").ShHighlightRules;
    var Range = require("../range").Range;

    var Mode = function () {
        this.$tokenizer = new Tokenizer(new ShHighlightRules().getRules());
    };
    oop.inherits(Mode, TextMode);

    (function () {

        this.toggleCommentLines = function (state, doc, startRow, endRow) {
            var outdent = true;
            var re = /^(\s*)#/;

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
                doc.indentRows(startRow, endRow, "#");
            }
        };

        this.getNextLineIndent = function (state, line, tab) {
            var indent = this.$getIndent(line);

            var tokenizedLine = this.$tokenizer.getLineTokens(line, state);
            var tokens = tokenizedLine.tokens;

            if (tokens.length && tokens[tokens.length - 1].type == "comment") {
                return indent;
            }

            if (state == "start") {
                var match = line.match(/^.*[\{\(\[\:]\s*$/);
                if (match) {
                    indent += tab;
                }
            }

            return indent;
        };

        var outdents = {
            "pass": 1,
            "return": 1,
            "raise": 1,
            "break": 1,
            "continue": 1
        };

        this.checkOutdent = function (state, line, input) {
            if (input !== "\r\n" && input !== "\r" && input !== "\n")
                return false;

            var tokens = this.$tokenizer.getLineTokens(line.trim(), state).tokens;

            if (!tokens)
                return false;
            do {
                var last = tokens.pop();
            } while (last && (last.type == "comment" || (last.type == "text" && last.value.match(/^\s+$/))));

            if (!last)
                return false;

            return (last.type == "keyword" && outdents[last.value]);
        };

        this.autoOutdent = function (state, doc, row) {

            row += 1;
            var indent = this.$getIndent(doc.getLine(row));
            var tab = doc.getTabString();
            if (indent.slice(-tab.length) == tab)
                doc.remove(new Range(row, indent.length - tab.length, row, indent.length));
        };

    }).call(Mode.prototype);

    exports.Mode = Mode;
});

define('ace/mode/sh_highlight_rules', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text_highlight_rules'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

    var ShHighlightRules = function () {

        var reservedKeywords = (
            '!|{|}|case|do|done|elif|else|' +
                'esac|fi|for|if|in|then|until|while|' +
                '&|;|export|local|read|typeset|unset|' +
                'elif|select|set'
            );

        var languageConstructs = (
            '[|]|alias|bg|bind|break|builtin|' +
                'cd|command|compgen|complete|continue|' +
                'dirs|disown|echo|enable|eval|exec|' +
                'exit|fc|fg|getopts|hash|help|history|' +
                'jobs|kill|let|logout|popd|printf|pushd|' +
                'pwd|return|set|shift|shopt|source|' +
                'suspend|test|times|trap|type|ulimit|' +
                'umask|unalias|wait'
            );

        var keywordMapper = this.createKeywordMapper({
            "keyword": reservedKeywords,
            "constant.language": languageConstructs,
            "invalid.deprecated": "debugger"
        }, "identifier");

        var integer = "(?:(?:[1-9]\\d*)|(?:0))";

        var fraction = "(?:\\.\\d+)";
        var intPart = "(?:\\d+)";
        var pointFloat = "(?:(?:" + intPart + "?" + fraction + ")|(?:" + intPart + "\\.))";
        var exponentFloat = "(?:(?:" + pointFloat + "|" + intPart + ")" + ")";
        var floatNumber = "(?:" + exponentFloat + "|" + pointFloat + ")";
        var fileDescriptor = "(?:&" + intPart + ")";

        var variableName = "[a-zA-Z][a-zA-Z0-9_]*";
        var variable = "(?:(?:\\$" + variableName + ")|(?:" + variableName + "=))";

        var builtinVariable = "(?:\\$(?:SHLVL|\\$|\\!|\\?))";

        var func = "(?:" + variableName + "\\s*\\(\\))";

        this.$rules = {
            "start": [
                {
                    token: "comment",
                    regex: "#.*$"
                },
                {
                    token: "string",           // " string
                    regex: '"(?:[^\\\\]|\\\\.)*?"'
                },
                {
                    token: "variable.language",
                    regex: builtinVariable
                },
                {
                    token: "variable",
                    regex: variable
                },
                {
                    token: "support.function",
                    regex: func
                },
                {
                    token: "support.function",
                    regex: fileDescriptor
                },
                {
                    token: "string",           // ' string
                    regex: "'(?:[^\\\\]|\\\\.)*?'"
                },
                {
                    token: "constant.numeric", // float
                    regex: floatNumber
                },
                {
                    token: "constant.numeric", // integer
                    regex: integer + "\\b"
                },
                {
                    token: keywordMapper,
                    regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"
                },
                {
                    token: "keyword.operator",
                    regex: "\\+|\\-|\\*|\\*\\*|\\/|\\/\\/|~|<|>|<=|=>|=|!="
                },
                {
                    token: "paren.lparen",
                    regex: "[\\[\\(\\{]"
                },
                {
                    token: "paren.rparen",
                    regex: "[\\]\\)\\}]"
                },
                {
                    token: "text",
                    regex: "\\s+"
                }
            ]
        };
    };

    oop.inherits(ShHighlightRules, TextHighlightRules);

    exports.ShHighlightRules = ShHighlightRules;
});
