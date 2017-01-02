/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

define('ace/mode/tcl', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text', 'ace/tokenizer', 'ace/mode/folding/cstyle', 'ace/mode/tcl_highlight_rules', 'ace/mode/matching_brace_outdent', 'ace/range'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var Tokenizer = require("../tokenizer").Tokenizer;
    var CStyleFoldMode = require("./folding/cstyle").FoldMode;
    var TclHighlightRules = require("./tcl_highlight_rules").TclHighlightRules;
    var MatchingBraceOutdent = require("./matching_brace_outdent").MatchingBraceOutdent;
    var Range = require("../range").Range;

    var Mode = function () {
        this.$tokenizer = new Tokenizer(new TclHighlightRules().getRules());
        this.$outdent = new MatchingBraceOutdent();
        this.foldingRules = new CStyleFoldMode();
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
                var match = line.match(/^.*[\{\(\[]\s*$/);
                if (match) {
                    indent += tab;
                }
            }

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

define('ace/mode/folding/cstyle', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/range', 'ace/mode/folding/fold_mode'], function (require, exports, module) {


    var oop = require("../../lib/oop");
    var Range = require("../../range").Range;
    var BaseFoldMode = require("./fold_mode").FoldMode;

    var FoldMode = exports.FoldMode = function () {
    };
    oop.inherits(FoldMode, BaseFoldMode);

    (function () {

        this.foldingStartMarker = /(\{|\[)[^\}\]]*$|^\s*(\/\*)/;
        this.foldingStopMarker = /^[^\[\{]*(\}|\])|^[\s\*]*(\*\/)/;

        this.getFoldWidgetRange = function (session, foldStyle, row) {
            var line = session.getLine(row);
            var match = line.match(this.foldingStartMarker);
            if (match) {
                var i = match.index;

                if (match[1])
                    return this.openingBracketBlock(session, match[1], row, i);

                return session.getCommentFoldRange(row, i + match[0].length, 1);
            }

            if (foldStyle !== "markbeginend")
                return;

            var match = line.match(this.foldingStopMarker);
            if (match) {
                var i = match.index + match[0].length;

                if (match[1])
                    return this.closingBracketBlock(session, match[1], row, i);

                return session.getCommentFoldRange(row, i, -1);
            }
        };

    }).call(FoldMode.prototype);

});

define('ace/mode/tcl_highlight_rules', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text_highlight_rules'], function (require, exports, module) {


    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

    var TclHighlightRules = function () {

        this.$rules = {
            "start": [
                {
                    token: "comment",
                    regex: "#.*\\\\$",
                    next: "commentfollow"
                },
                {
                    token: "comment",
                    regex: "#.*$"
                },
                {
                    token: "support.function",
                    regex: '[\\\\]$',
                    next: "splitlineStart"
                },
                {
                    token: "text",
                    regex: '[\\\\](?:["]|[{]|[}]|[[]|[]]|[$]|[\])'
                },
                {
                    token: "text", // last value before command
                    regex: '^|[^{][;][^}]|[/\r/]',
                    next: "commandItem"
                },
                {
                    token: "string", // single line
                    regex: '[ ]*["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]'
                },
                {
                    token: "string", // multi line """ string start
                    regex: '[ ]*["]',
                    next: "qqstring"
                },
                {
                    token: "variable.instance", // variable xotcl with braces
                    regex: "[$]",
                    next: "variable"
                },
                {
                    token: "support.function",
                    regex: "!|\\$|%|&|\\*|\\-\\-|\\-|\\+\\+|\\+|~|===|==|=|!=|!==|<=|>=|<<=|>>=|>>>=|<>|<|>|!|&&|\\|\\||\\?\\:|\\*=|%=|\\+=|\\-=|&=|\\^=|{\\*}|;|::"
                },
                {
                    token: "identifier",
                    regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"
                },
                {
                    token: "paren.lparen",
                    regex: "[[{]",
                    next: "commandItem"
                },
                {
                    token: "paren.lparen",
                    regex: "[(]"
                },
                {
                    token: "paren.rparen",
                    regex: "[\\])}]"
                },
                {
                    token: "text",
                    regex: "\\s+"
                }
            ],
            "commandItem": [
                {
                    token: "comment",
                    regex: "#.*\\\\$",
                    next: "commentfollow"
                },
                {
                    token: "comment",
                    regex: "#.*$",
                    next: "start"
                },
                {
                    token: "string", // single line
                    regex: '[ ]*["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]'
                },
                {
                    token: "variable.instance", // variable xotcl with braces
                    regex: "[$]",
                    next: "variable"
                },
                {
                    token: "support.function",
                    regex: "(?:[:][:])[a-zA-Z0-9_/]+(?:[:][:])",
                    next: "commandItem"
                },
                {
                    token: "support.function",
                    regex: "[a-zA-Z0-9_/]+(?:[:][:])",
                    next: "commandItem"
                },
                {
                    token: "support.function",
                    regex: "(?:[:][:])",
                    next: "commandItem"
                },
                {
                    token: "paren.rparen",
                    regex: "[\\])}]"
                },
                {
                    token: "support.function",
                    regex: "!|\\$|%|&|\\*|\\-\\-|\\-|\\+\\+|\\+|~|===|==|=|!=|!==|<=|>=|<<=|>>=|>>>=|<>|<|>|!|&&|\\|\\||\\?\\:|\\*=|%=|\\+=|\\-=|&=|\\^=|{\\*}|;|::"
                },
                {
                    token: "keyword",
                    regex: "[a-zA-Z0-9_/]+",
                    next: "start"
                }
            ],
            "commentfollow": [
                {
                    token: "comment",
                    regex: ".*\\\\$",
                    next: "commentfollow"
                },
                {
                    token: "comment",
                    regex: '.+',
                    next: "start"
                }
            ],
            "splitlineStart": [
                {
                    token: "text",
                    regex: "^.",
                    next: "start"
                }
            ],
            "variable": [
                {
                    token: "variable.instance", // variable xotcl with braces
                    regex: "(?:[:][:])?[a-zA-Z_\\d]+(?:(?:[:][:])?[a-zA-Z_\\d]+)?(?:[(][a-zA-Z_\\d]+[)])?",
                    next: "start"
                },
                {
                    token: "variable.instance", // variable tcl
                    regex: "[a-zA-Z_\\d]+(?:[(][a-zA-Z_\\d]+[)])?",
                    next: "start"
                },
                {
                    token: "variable.instance", // variable tcl with braces
                    regex: "{?[a-zA-Z_\\d]+}?",
                    next: "start"
                }
            ],
            "qqstring": [
                {
                    token: "string", // multi line """ string end
                    regex: '(?:[^\\\\]|\\\\.)*?["]',
                    next: "start"
                },
                {
                    token: "string",
                    regex: '.+'
                }
            ]
        };
    };

    oop.inherits(TclHighlightRules, TextHighlightRules);

    exports.TclHighlightRules = TclHighlightRules;
});

define('ace/mode/matching_brace_outdent', ['require', 'exports', 'module' , 'ace/range'], function (require, exports, module) {


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
