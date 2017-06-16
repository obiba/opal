//RQL implemented without Dojo
//original Dojo implementation:  https://github.com/kriszyp/rql

//creates three classes:  RqlParser, RqlQuery, RqlArray
//main usage is RqlArray.executeQuery(query, options, data)
//the other two classes are used by RqlArray to parse and execute the queries.
//
//basic example
// 
//   var ra = new RqlArray(),
//       data = [{name: "Danbo", age: 25}, {name: "Jimbo", age: 45}, {name: "Hambo", age: 31}],
//       query = "lt(age,40)",
//       result;
//   result = ra.executeQuery(query, {}, data);
//
// the contents of result would be  [{name: "Danbo", age: 25}, {name: "Hambo", age: 31}]


//---------------------------
// Parser Class
//
// used to parse query strings into query objects 
//---------------------------

//object constructer
var RqlParser = function () {
    var that = this;

    this.operatorMap = {
        "=": "eq",
        "==": "eq",
        ">": "gt",
        ">=": "ge",
        "<": "lt",
        "<=": "le",
        "!=": "ne"
    };

    this.commonOperatorMap = {
        "and": "&",
        "or": "|",
        "eq": "=",
        "ne": "!=",
        "le": "<=",
        "ge": ">=",
        "lt": "<",
        "gt": ">"
    };

    this.primaryKeyName = 'id';
    this.lastSeen = ['sort', 'select', 'values', 'limit'];
    this.jsonQueryCompatible = true;

    this.autoConverted = {
        "true": true,
        "false": false,
        "null": null,
        "undefined": undefined,
        "Infinity": Infinity,
        "-Infinity": -Infinity
    };

    //big mapping of converter functions (functions that convert data to values in their proper data type)
    this.converters = {
        //auto. attempt basic conversion for keywords, numbers, strings, dates
        auto: function (string) {
            //keywords
            if (that.autoConverted.hasOwnProperty(string)) {
                return that.autoConverted[string];
            }
            //number check
            var number = +string;

            if (isNaN(number) || number.toString() !== string) {
                string = decodeURIComponent(string);
                if (that.jsonQueryCompatible) {
                    //if wrapped in single quotes, switch to be wrapped in double quotes. then parse as JSON
                    if (string.charAt(0) == "'" && string.charAt(string.length - 1) == "'") {
                        return JSON.parse('"' + string.substring(1, string.length - 1) + '"');
                    }
                }
                //string
                return string;
            }
            //number
            return number;
        },
        number: function (x) {
            var number = +x;
            if (isNaN(number)) {
                throw new URIError("Invalid number " + number);
            }
            return number;
        },
        epoch: function (x) {
            var date = new Date(+x);
            if (isNaN(date.getTime())) {
                throw new URIError("Invalid date " + x);
            }
            return date;
        },
        isodate: function (x) {
            // four-digit year
            var date = '0000'.substr(0, 4 - x.length) + x;
            // pattern for partial dates
            date += '0000-01-01T00:00:00Z'.substring(date.length);
            return that.converters.date(date);
        },
        date: function (x) {
            var isoDate = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}(?:\.\d*)?)Z$/.exec(x),
				dDate;
            if (isoDate) {
                dDate = new Date(Date.UTC(+isoDate[1], +isoDate[2] - 1, +isoDate[3], +isoDate[4], +isoDate[5], +isoDate[6]));
            } else {
                dDate = new Date(x);
            }
            if (isNaN(dDate.getTime())) {
                throw new URIError("Invalid date " + x);
            }
            return dDate;
        },
        "boolean": function (x) {
            return x === "true";
        },
        string: function (string) {
            return decodeURIComponent(string);
        },
        re: function (x) {
            return new RegExp(decodeURIComponent(x), 'i');
        },
        RE: function (x) {
            return new RegExp(decodeURIComponent(x));
        },
        glob: function (x) {
            var s = decodeURIComponent(x).replace(/([\\|\||\(|\)|\[|\{|\^|\$|\*|\+|\?|\.|\<|\>])/g, function (x) { return '\\' + x; }).replace(/\\\*/g, '.*').replace(/\\\?/g, '.?');
            if (s.substring(0, 2) !== '.*') s = '^' + s; else s = s.substring(2);
            if (s.substring(s.length - 2) !== '.*') s = s + '$'; else s = s.substring(0, s.length - 2);
            return new RegExp(s, 'i');
        }
    };

    //set the default converter to the auto-converter function
    this.converters["default"] = this.converters.auto;
};

//main parse function
RqlParser.prototype.parse = function (query, parameters) {
    //init bad input
    if (typeof query === "undefined" || query === null) {
        query = '';
    }

    var that = this;
    var term = new RqlQuery();
    var topTerm = term;
    topTerm.cache = {}; // room for lastSeen params

    //lets start parsing this query
    if (typeof query === "object") {
        if (query instanceof RqlQuery) {
            //its already parsed!
            return query;
        }
        //not sure what this does.
        //if query is an object, but not an RqlQuery, go through the object properties and turn them into query terms.
        //TODO revisit with better explanation. it may be this is not used when parsing string-based queries, only funny chained functions
        for (var i in query) {
            var qTerm = new RqlQuery();
            topTerm.args.push(qTerm);
            qTerm.name = "eq";
            qTerm.args = [i, query[i]]; //property-value pair array
        }
        return topTerm;
    }

    //I WILL NOT TOLERATE YOUR QUESTION MARK INSOLENCE
    if (query.charAt(0) == "?") {
        throw new URIError("Query must not start with ?");
    }

    //as far as i can tell, jsonQueryCompatible is always true
    //replace angle bracket symbols ( >= <= > < ) with text formula equivalents
    if (this.jsonQueryCompatible) {
        query = query.replace(/%3C=/g, "=le=").replace(/%3E=/g, "=ge=").replace(/%3C/g, "=lt=").replace(/%3E/g, "=gt=");
    }

    if (query.indexOf("/") > -1) { // performance guard
        // convert slash delimited text to arrays
        query = query.replace(/[\+\*\$\-:\w%\._]*\/[\+\*\$\-:\w%\._\/]*/g, function (slashed) {
            return "(" + slashed.replace(/\//g, ",") + ")";
        });
    }

    // convert FIQL to normalized call syntax form
    query = query.replace(/(\([\+\*\$\-:\w%\._,]+\)|[\+\*\$\-:\w%\._]*|)([<>!]?=(?:[\w]*=)?|>|<)(\([\+\*\$\-:\w%\._,]+\)|[\+\*\$\-:\w%\._]*|)/g,
	                     //<---------       property        -----------><------  operator -----><----------------   value ------------------>
			function (t, property, operator, value) {
			    if (operator.length < 3) {
			        if (!that.operatorMap[operator]) {
			            throw new URIError("Illegal operator " + operator);
			        }
			        operator = that.operatorMap[operator];
			    }
			    else {
			        operator = operator.substring(1, operator.length - 1);
			    }
			    return operator + '(' + property + "," + value + ")";
			});

    //STILL TRYING TO QUESTION MARK, EH? I HOP OVER YOUR SILLYNESS
    if (query.charAt(0) == "?") {
        query = query.substring(1);
    }

    //get leftover chars from the query
    //this could be things like more and/ors, or a comma to apply a sort or filter
    var leftoverCharacters = query.replace(/(\))|([&\|,])?([\+\*\$\-:\w%\._]*)(\(?)/g,
	                       //    <-closedParan->|<-delim-- propertyOrValue -----(> |
		function (t, closedParan, delim, propertyOrValue, openParan) {
		    if (delim) {
		        if (delim === "&") {
		            setConjunction("and");
		        }
		        if (delim === "|") {
		            setConjunction("or");
		        }
		    }
		    if (openParan) {
		        var newTerm = new RqlQuery();
		        newTerm.name = propertyOrValue;
		        newTerm.parent = term;
		        call(newTerm);
		    }
		    else if (closedParan) {
		        var isArray = !term.name;
		        term = term.parent;
		        if (!term) {
		            throw new URIError("Closing paranthesis without an opening paranthesis");
		        }
		        if (isArray) {
		            term.args.push(term.args.pop().args);
		        }
		    }
		    else if (propertyOrValue || delim === ',') {
		        term.args.push(that.stringToValue(propertyOrValue, parameters));

		        // cache the last seen sort(), select(), values() and limit()
		        if (that.contains(that.lastSeen, term.name)) {
		            topTerm.cache[term.name] = term.args;
		        }
		        // cache the last seen id equality
		        if (term.name === 'eq' && term.args[0] === that.primaryKeyName) {
		            var id = term.args[1];
		            if (id && !(id instanceof RegExp)) id = id.toString();
		            topTerm.cache[that.primaryKeyName] = id;
		        }
		    }
		    return "";
		});

    //balanced paranthesis check
    if (term.parent) {
        throw new URIError("Opening paranthesis without a closing paranthesis");
    }

    //if we found leftover chars, get angry
    if (leftoverCharacters) {
        // any extra characters left over from the replace indicates invalid syntax
        throw new URIError("Illegal character in query string encountered " + leftoverCharacters);
    }

    //worker bee functions

    function call(newTerm) {
        term.args.push(newTerm);
        term = newTerm;
        // cache the last seen sort(), select(), values() and limit()
        if (that.contains(that.lastSeen, term.name)) {
            topTerm.cache[term.name] = term.args;
        }
    }
    function setConjunction(operator) {
        if (!term.name) {
            term.name = operator;
        }
        else if (term.name !== operator) {
            throw new Error("Can not mix conjunctions within a group, use paranthesis around each set of same conjuctions (& and |)");
        }
    }
    function removeParentProperty(obj) {
        if (obj && obj.args) {
            delete obj.parent;
            var args = obj.args;
            for (var i = 0, l = args.length; i < l; i++) {
                removeParentProperty(args[i]);
            }
        }
        return obj;
    };

    //remove the parent properties from our query tree
    removeParentProperty(topTerm);

    //return a nicely parsed thing
    return topTerm;
};

//contains function
//TODO can this be replaced by a simple built-in array function, like indexOf?
RqlParser.prototype.contains = function (array, item) {
    for (var i = 0, l = array.length; i < l; i++) {
        if (array[i] === item) {
            return true;
        }
    }
};

//parseGently function
// dumps undesirable exceptions to RqlQuery().error
RqlParser.prototype.parseGently = function () {
    var terms;
    try {
        terms = this.parse.apply(this, arguments);
    } catch (err) {
        terms = new RqlQuery();
        terms.error = err.message;
    }
    return terms;
};

//stringToValue function
//converts string representations of things into values of the proper data type
RqlParser.prototype.stringToValue = function (str, parameters) {
    var converter = this.converters['default'];
    if (str.charAt(0) === "$") {
        var param_index = parseInt(str.substring(1)) - 1;
        return param_index >= 0 && parameters ? parameters[param_index] : undefined;
    }
    if (str.indexOf(":") > -1) {
        var parts = str.split(":", 2);
        converter = this.converters[parts[0]];
        if (!converter) {
            throw new URIError("Unknown converter " + parts[0]);
        }
        str = parts[1];
    }
    return converter(str);
};

//---------------------------
// Query Class
//
// Encodes a query in a form that can be evaluated easily 
//---------------------------

//object constructer
var RqlQuery = function (name) {
    this.name = name || "and";
    this.args = [];
    this.knownOperators = ["sort", "in", "not", "any", "all", "or", "and", "select", "exclude", "values", "limit", "distinct", "recurse", "aggregate", "between", "sum", "mean", "max", "min", "count", "first", "one", "eq", "ne", "le", "ge", "lt", "gt"];
    this.knownScalarOperators = ["mean", "sum", "min", "max", "count", "first", "one"];
    this.arrayMethods = ["forEach", "reduce", "map", "filter", "indexOf", "some", "every"];
    this.parser = new RqlParser();
};

//when function
//TODO may need to adopt the promised-io libary.  Would hope not; in our usage everything should be synchronous
RqlQuery.prototype.when = function (value, callback) {
    callback(value);
};

//serializeArgs function
RqlQuery.prototype.serializeArgs = function serializeArgs(array, delimiter) {
    var results = [];
    for (var i = 0, l = array.length; i < l; i++) {
        results.push(this.queryToString(array[i]));
    }
    return results.join(delimiter);
};

//toString function
RqlQuery.prototype.toString = function () {
    return this.name === "and" ?
		this.serializeArgs(this.args, "&") :
		this.queryToString(this);
};

//queryToString function
RqlQuery.prototype.queryToString = function (part) {
    if (part instanceof Array) {
        return '(' + this.serializeArgs(part, ",") + ')';
    }
    if (part && part.name && part.args) {
        return [
                part.name,
                "(",
                this.serializeArgs(part.args, ","),
                ")"
        ].join("");
    }
    return this.encodeValue(part);
};

//encodeString function
RqlQuery.prototype.encodeString = function (s) {
    if (typeof s === "string") {
        s = encodeURIComponent(s);
        if (s.match(/[\(\)]/)) {
            s = s.replace("(", "%28").replace(")", "%29");
        };
    }
    return s;
};

//encodeValue function
RqlQuery.prototype.encodeValue = function (val) {
    var encoded;
    if (val === null) { val = 'null'; }
    if (val !== this.parser.converters["default"]('' + (val.toISOString && val.toISOString() || val.toString()))) {
        var type = typeof val;
        if (val instanceof RegExp) {
            // TODO: control whether to we want simpler glob() style
            val = val.toString();
            var i = val.lastIndexOf('/');
            type = val.substring(i).indexOf('i') >= 0 ? "re" : "RE";
            val = this.encodeString(val.substring(1, i));
            encoded = true;
        }
        if (type === "object") {
            type = "epoch";
            val = val.getTime();
            encoded = true;
        }
        if (type === "string") {
            val = this.encodeString(val);
            encoded = true;
        }
        val = [type, val].join(":");
    }
    if (!encoded && typeof val === "string") { val = this.encodeString(val); }
    return val;
};

//updateQueryMethods function
//sets up functions on the RqlQuery object to execute various query-string operators & methods
RqlQuery.prototype.updateQueryMethods = function () {
    var that = this;
    this.knownOperators.forEach(function (name) {
        RqlQuery.prototype[name] = function () {
            var newQuery = new RqlQuery(undefined);
            newQuery.executor = that.executor;
            var newTerm = new RqlQuery(name);
            newTerm.args = Array.prototype.slice.call(arguments);
            newQuery.args = that.args.concat([newTerm]);
            return newQuery;
        };
    });
    this.knownScalarOperators.forEach(function (name) {
        RqlQuery.prototype[name] = function () {
            var newQuery = new RqlQuery(undefined);
            newQuery.executor = that.executor;
            var newTerm = new RqlQuery(name);
            newTerm.args = Array.prototype.slice.call(arguments);
            newQuery.args = that.args.concat([newTerm]);
            return newQuery.executor(newQuery);
        };
    });
    this.arrayMethods.forEach(function (name) {
        RqlQuery.prototype[name] = function () {
            var args = arguments;
            return that.when(that.executor(that), function (results) {
                return results[name].apply(results, args);
            });
        };
    });
};

//walk function
// recursively iterate over query terms calling 'fn' for each term
RqlQuery.prototype.walk = function (fn, options) {
    options = options || {};
    function walk(name, terms) {
        (terms || []).forEach(function (term, i, arr) {
            var args, func;
            term != null ? term : term = {};
            func = term.name;
            args = term.args;
            if (!func || !args) {
                return;
            }
            if (args[0] instanceof RqlQuery) {
                walk.call(this, func, args);
            } else {
                var newTerm = fn.call(this, func, args);
                if (newTerm && newTerm.name && newTerm.args)
                    arr[i] = newTerm;
            }
        });
    }
    walk.call(this, this.name, this.args);
};

//push function
// append a new term
RqlQuery.prototype.push = function (term) {
    this.args.push(term);
    return this;
};

//normalize function
/* disambiguate query */
RqlQuery.prototype.normalize = function (options) {
    options = options || {};
    options.primaryKey = options.primaryKey || 'id';
    options.map = options.map || {};
    var result = {
        original: this,
        sort: [],
        limit: [Infinity, 0, Infinity],
        skip: 0,
        limit: Infinity,
        select: [],
        values: false
    };
    var plusMinus = {
        // [plus, minus]
        sort: [1, -1],
        select: [1, 0]
    };
    function normal(func, args) {
        // cache some parameters
        if (func === 'sort' || func === 'select') {
            result[func] = args;
            var pm = plusMinus[func];
            result[func + 'Arr'] = result[func].map(function (x) {
                if (x instanceof Array) {
                    x = x.join('.');
                }
                var o = {};
                var a = /([-+]*)(.+)/.exec(x);
                o[a[2]] = pm[((a[1].charAt(0) === '-') ? 1 : 0) * 1];
                return o;
            });
            result[func + 'Obj'] = {};
            result[func].forEach(function (x) {
                if (x instanceof Array) x = x.join('.');
                var a = /([-+]*)(.+)/.exec(x);
                result[func + 'Obj'][a[2]] = pm[((a[1].charAt(0) === '-') ? 1 : 0) * 1];
            });
        } else if (func === 'limit') {
            // validate limit() args to be numbers, with sane defaults
            var limit = args;
            result.skip = +limit[1] || 0;
            limit = +limit[0] || 0;
            if (options.hardLimit && limit > options.hardLimit)
                limit = options.hardLimit;
            result.limit = limit;
            result.needCount = true;
        } else if (func === 'values') {
            // N.B. values() just signals we want array of what we select()
            result.values = true;
        } else if (func === 'eq') {
            // cache primary key equality -- useful to distinguish between .get(id) and .query(query)
            var t = typeof args[1];
            //if ((args[0] instanceof Array ? args[0][args[0].length-1] : args[0]) === options.primaryKey && ['string','number'].indexOf(t) >= 0) {
            if (args[0] === options.primaryKey && ['string', 'number'].indexOf(t) >= 0) {
                result.pk = String(args[1]);
            }
        }
    }
    this.walk(normal);
    return result;
};

//---------------------------
// Array Class
//
// contains implementation of array-based query functions, and converts a query object into 
// javascript that can be applied on an array of data
//---------------------------

//object constructer
var RqlArray = function () {
    var that = this;

    //this.parseQuery = new RqlParser();
    this.nextId = 1;
    this.jsOperatorMap = {
        "eq": "===",
        "ne": "!==",
        "le": "<=",
        "ge": ">=",
        "lt": "<",
        "gt": ">"
    };

    //big set of operator functions
    //inside all operator functions, 'this' refers to the array of data that has been passed in to RqlArray.executeQuery
    this.operators = {
        //will sort an array based on a property value. a leading plus or minus will dictate the direction of the sort
        // e.g. sort(+lastName)
        //can be applied after a filter
        // e.g. lt(age,40),sort(-age)
        sort: function () {
            var terms = [];
            for (var i = 0; i < arguments.length; i++) {
                var sortAttribute = arguments[i];
                var firstChar = sortAttribute.charAt(0);
                var term = { attribute: sortAttribute, ascending: true };
                if (firstChar == "-" || firstChar == "+") {
                    if (firstChar == "-") {
                        term.ascending = false;
                    }
                    term.attribute = term.attribute.substring(1);
                }
                terms.push(term);
            }
            //sort the array of data
            this.sort(function (a, b) {
                for (var term, i = 0; term = terms[i]; i++) {
                    if (a[term.attribute] != b[term.attribute]) {
                        return term.ascending == a[term.attribute] > b[term.attribute] ? 1 : -1;
                    }
                }
                return 0;
            });
            return this;
        },
        //will return true if a property value matches a regex pattern with case insensitivity
        // e.g. match(firstName,bi)
        match: that.filter(function (value, regex) {            
            return new RegExp(regex, 'i').test(value);
        }),
        //will return true if a property value matches a regex pattern with case sensitivity
        // e.g. matchcase(firstName,Bi)
        matchcase: that.filter(function (value, regex) {
            return new RegExp(regex).test(value);
        }),
        //will return true if a property has a value matching any value in the second parameter (which is an array of values).
        //essentially a shortcut for a string of equality checks separated by ORs
        // e.g. in(firstName,(John,Johnny,Jon))
        "in": that.filter(function (value, values) {
            return that.contains(values, value);
        }),
        //will return true if a property has a value not matching any value in the second parameter (which is an array of values).
        //essentially a shortcut for a string of equality checks separated by ORs, then negated
        // e.g. out(firstName,(Jimbo,Danbo,Hankbo))
        out: that.filter(function (value, values) {
            return !that.contains(values, value);
        }),
        //used for inspecting array properties, will return true if array has a value in it
        // e.g. contains(colours,blue)
        //where colours is an array property of the data objects e.g. [{colours:['green', 'red']},{colours:['green', 'blue']}]
        //the value can also be a function that applies a filter to elements of the array.
        // e.g. contains(paints,eq(colour,blue))
        //where the source data could be [{paints:[{size:1, colour:'blue'}, {size:8, colour:'green'}]}, {paints:[{size:3, colour:'red'}]}]
        contains: that.filter(function (array, value) {
            if (typeof value == "function") {
                return array instanceof Array && that.each(array, function (v) {
                    return value.call([v]).length;
                });
            }
            else {
                return array instanceof Array && that.contains(array, value);
            }
        }),
        //used for inspecting array properties, will return true if array does not have a value in it
        // e.g. excludes(colours,blue)
        //where colours is an array property of the data objects e.g. [{colours:['green', 'red']}, {colours:['green', 'blue']}]
        excludes: that.filter(function (array, value) {
            if (typeof value == "function") {
                return !that.each(array, function (v) {
                    return value.call([v]).length;
                });
            }
            else {
                return !that.contains(array, value);
            }
        }),
        //will apply OR logic to any number of conditions
        //e.g. or(eq(firstName,John),eq(firstName,Johnny),eq(firstName,Jon))
        or: function () {

           
            var items = [];
            var idProperty = "__rqlId" + that.nextId++;
            try {
                for (var i = 0; i < arguments.length; i++) {
                    //apply each 'or' test against the data
                    var group = arguments[i].call(this);
                    for (var j = 0, l = group.length; j < l; j++) {
                        var item = group[j];
                        // use marker to do a union in linear time.
                        if (!item[idProperty]) {
                            item[idProperty] = true;
                            items.push(item);
                        }
                    }
                }
            } finally {
                // cleanup markers
                //TODO figure out what this code is doing?
                for (var i = 0, l = items.length; i < l; i++) {
                    //JR - orig version did not target the index.  confirmed problem existed in original dojo version 
                    //delete items[idProperty];
                    delete items[i][idProperty];
                }
            }
            return items;
            
            
            //alternate version taken from http://rql-engine.eu01.aws.af.cm/
            /*
              var items = [];
              //TODO: remove duplicates and use condition property
              for ( var i = 0; i < arguments.length; i++) {
            	items = items.concat(arguments[i].call(this));
              }
              return items;
            */
            
        },
        //will apply AND logic to any number of conditions
        //e.g. and(lt(age,50),eq(firstName,Jake),eq(lastName,Chambers))
        and: function () {
            var items = this; //the array of data
            // TODO: use condition property
            for (var i = 0; i < arguments.length; i++) {
                items = arguments[i].call(items);
            }
            return items;
        },
        //will return objects with only the given properties
        //e.g. select(firstName,lastName)
        //when applied on an array of "person" objects with many properties, will return array of objects with only firstName and lastName properties
        select: function () {
            var args = arguments;
            var argc = arguments.length;
            return that.each(this, function (object, emit) {
                var selected = {};
                for (var i = 0; i < argc; i++) {
                    var propertyName = args[i];
                    var value = that.evaluateProperty(object, propertyName);
                    if (typeof value != "undefined") {
                        selected[propertyName] = value;
                    }
                }
                emit(selected);
            });
        },
        //will return objects with the given properties removed
        //e.g. unselect(firstName,lastName)
        //when applied on an array of "person" objects with many properties, will return array of objects with firstName and lastName properties removed
        unselect: function () {
            var args = arguments;
            var argc = arguments.length;
            return that.each(this, function (object, emit) {
                var selected = {};
                for (var i in object) if (object.hasOwnProperty(i)) {
                    selected[i] = object[i];
                }
                for (var i = 0; i < argc; i++) {
                    delete selected[args[i]];
                }
                emit(selected);
            });
        },
        //will return an array of values for a given property.  if multiple properties are given, will return arrays of values for each data item
        // e.g. values(firstName)
        // could return ['Roland','Susannah','Eddie']
        // e.g. values(firstName,lastName)
        // could return [['Roland','Deschain'],['Susannah','Dean'],['Eddie','Dean']]
        values: function (first) {
            if (arguments.length == 1) {
                return that.each(this, function (object, emit) {
                    emit(object[first]);
                });
            }
            var args = arguments;
            var argc = arguments.length;
            return that.each(this, function (object, emit) {
                var selected = [];
                if (argc === 0) {
                    for (var i in object) if (object.hasOwnProperty(i)) {
                        selected.push(object[i]);
                    }
                } else {
                    for (var i = 0; i < argc; i++) {
                        var propertyName = args[i];
                        selected.push(object[propertyName]);
                    }
                }
                emit(selected);
            });
        },
        //will return a subsection of the data array.  first parameter is number of items to return. second parameter is index to start taking at.
        //unclear what third parameter 'maxCount' does. it adds extra properties to the result, but nothing seems to use them
        // e.g. limit(10,4)
        // will return array obects 5 through 14
        limit: function (limit, start, maxCount) {
            var totalCount = this.length;
            start = start || 0;
            var sliced = this.slice(start, start + limit);
            if (maxCount) {
                sliced.start = start;
                sliced.end = start + sliced.length - 1;
                sliced.totalCount = Math.min(totalCount, typeof maxCount === "number" ? maxCount : Infinity);
            }
            return sliced;
        },
        //returns the array of values with any duplicates removed
        //does not appear to work on complex objects
        // e.g. distinct()
        //can come after a filter
        // e.g. values(lastName),distinct()
        distinct: function () {
            var primitives = {};
            var needCleaning = [];
            var newResults = this.filter(function (value) {
                if (value && typeof value == "object") {
                    if (!value.__found__) {
                        value.__found__ = function () { };// get ignored by JSON serialization
                        needCleaning.push(value);
                        return true;
                    }
                } else {
                    if (!primitives[value]) {
                        primitives[value] = true;
                        return true;
                    }
                }
            });
            that.each(needCleaning, function (object) {
                delete object.__found__;
            });
            return newResults;
        },        
        //flattens out nested arrays
        // e.g. input [[1,2,3],[[4,5],[6,7]]]   will return  [1,2,3,4,5,6,7]
        //will also extract and flatten any arrays that are found under the given property
        // e.g. input  [{"name":"Roland", "orders":[{"id": 25}, {"id":40}]}, {"name":"Jake", "orders":[{"id": 19}]}] 
        //      query  recurse(orders)    
        //      result [{"name":"Roland", "orders":[{"id": 25}, {"id":40}]}, {"id": 25}, {"id":40}, {"name":"Jake", "orders":[{"id": 19}]}, {"id": 19}]   
        recurse: function (property) {
            // TODO: this needs to use lazy-array
            var newResults = [];          
            function recurse(value) {
                if (value instanceof Array) {
                    that.each(value, recurse);
                } else {
                    newResults.push(value);
                    if (property) {
                        value = value[property];
                        if (value && typeof value == "object") {
                            recurse(value);
                        }
                    } else {
                        for (var i in value) {
                            if (value[i] && typeof value[i] == "object") {
                                recurse(value[i]);
                            }
                        }
                    }
                }
            }
            recurse(this);
            return newResults;
        },
        //returns aggregations on the array.
        //parameters are list of values to group by, and list of functions to aggregate over the data.
        //functions should be appropriate.  i.e. they are applied against an array and return a scalar.
        //good functions: sum, count, first, max, min, mean 
        // e.g. aggregate(age,mean(salary))
        // e.g. aggregate(age,gender,mean(salary),count())
        //aggregation results are stored in numerically named properties in order of how they are defined in the function.
        // e.g. aggregate(age,mean(salary),count())
        //   could return something like [{"age":20, "0":53216, "1":13},{"age":21, "0":55898, "1":11},...]
        //can come after a filter
        // e.g. lt(age,50),aggregate(age,mean(salary))
        aggregate: function () {
            var distinctives = [];
            var aggregates = [];
            //figure out the parameters. functions are for aggregatin'. values are for grouping
            for (var i = 0; i < arguments.length; i++) {
                var arg = arguments[i];
                if (typeof arg === "function") {
                    aggregates.push(arg);
                } else {
                    distinctives.push(arg);
                }
            }
            var distinctObjects = {};
            var dl = distinctives.length;
            //go through all array objects and group thing together based on values of grouping properties
            that.each(this, function (object) {
                var key = "";
                for (var i = 0; i < dl; i++) {
                    key += '/' + object[distinctives[i]];
                }
                var arrayForKey = distinctObjects[key];
                if (!arrayForKey) {
                    arrayForKey = distinctObjects[key] = [];
                }
                arrayForKey.push(object);
            });
            var al = aggregates.length;
            var newResults = [];
            //call the aggregation functions on each unique grouping.
            //put all the function results (and grouping values) into result objects
            for (var key in distinctObjects) {
                var arrayForKey = distinctObjects[key];
                var newObject = {};
                for (var i = 0; i < dl; i++) {
                    var property = distinctives[i];
                    newObject[property] = arrayForKey[0][property];
                }
                for (var i = 0; i < al; i++) {
                    var aggregate = aggregates[i];
                    newObject[i] = aggregate.call(arrayForKey);
                }
                newResults.push(newObject);
            }
            return newResults;
        },
        //returns elements that are between a range. range can be of different types (though object type gets a little wonky)
        // e.g. between(age,(20,30))
        // e.g. between(lastName,(Ma,Mo))
        // e.g. between(,(100,200))   <-- works for array of values e.g. [100,200,300,400]
        between: that.filter(function (value, range) {
            return value >= range[0] && value < range[1];
        }),        
        //returns the sum of the value in the array, or in a property of the array
        //value used must be numeric
        // e.g. sum()    <-- only works for array of numerics e.g. [1,2,3,4]
        // e.g. sum(age)
        //can come after a filter
        // e.g. gt(age,20),sum(age)
        sum: that.reducer(function (a, b) {
            //adds up array using reducer, which applies a+b along each array element
            return a + b;
        }),
        //returns the mean average value in the array, or in a property of the array
        //value used must be numeric
        // e.g. mean()    <-- only works for array of numerics e.g. [1,2,3,4]
        // e.g. mean(age)
        //can come after a filter
        // e.g. gt(age,20),mean(age)
        mean: function (property) {
            return that.operators.sum.call(this, property) / this.length;
        },
        //returns the maximum value in the array, or in a property of the array
        //value used must be numeric
        // e.g. max()    <-- only works for array of numerics e.g. [1,2,3,4]
        // e.g. max(age)
        //can come after a filter
        // e.g. lt(age,65),max(age)
        max: that.reducer(function (a, b) {
            return Math.max(a, b);
        }),
        //returns the minimum value in the array, or in a property of the array
        //value used must be numeric
        // e.g. min()    <-- only works for array of numerics e.g. [1,2,3,4]
        // e.g. min(age)
        //can come after a filter
        // e.g. gt(age,20),min(age)
        min: that.reducer(function (a, b) {
            return Math.min(a, b);
        }),
        //returns the number of elements in the data array
        //important to note the result is not contained in an array.
        // e.g. count()
        // e.g. input ["hello", "goodbye"] , result will be 2, not [2]      
        //can come after a filter
        // e.g. eq(lastName,Dean),count()  
        count: function () {
            return this.length;
        },
        //returns the first element of the data array
        //important to note the result is not contained in an array.
        // e.g. first()
        // e.g. input ["hello", "goodbye"] , result will be "hello", not ["hello"]      
        //can come after a filter
        // e.g. values(lastName),first()  
        first: function () {
            return this[0];
        },
        //returns the only element of the data array, or an error if more than one
        //important to note the result is not contained in an array.
        // e.g. one()
        // e.g. input ["hello"] , result will be "hello", not ["hello"]      
        //can come after a filter
        // e.g. values(lastName),one()  
        one: function () {
            if (this.length > 1) {
                throw new TypeError("More than one object found");
            }
            return this[0];
        }
    };
};

//each function
//applies callback on all items of an array.  callback functions pass results back using an emit function
RqlArray.prototype.each = function (array, callback) {
    var emit, result;
    if (callback.length > 1) {
        // can take a second param, emit
        result = [];
        emit = function (value) {
            result.push(value);
        }
    }
    for (var i = 0, l = array.length; i < l; i++) {
        if (callback(array[i], emit)) {
            return result || true;
        }
    }
    return result;
};

//contains function
RqlArray.prototype.contains = function (array, item) {
    for (var i = 0, l = array.length; i < l; i++) {
        if (array[i] === item) {
            return true;
        }
    }
};

//stringify	 function
//use JSON object function if it is defined.  else use custom function
RqlArray.prototype.stringify = typeof JSON !== "undefined" && JSON.stringify || function (str) {
    return '"' + str.replace(/"/g, "\\\"") + '"';
};

//filter function
//filters out any array items that do not satisfy the condition function

//     may need to add an array parameter, as 'this' may get jibbed now that its part of the prototype.
//     as originally written, 'this' would refer to the data array being filtered
//     update: seems to be working.  i think the 'this' inside the inner function is not affected by the prototype.  the 'this' outside is. 
RqlArray.prototype.filter = function (condition, not) {
    var that = this;
    // convert to boolean right now
    var filter = function (property, second) {
        if (typeof second == "undefined") {
            second = property;
            property = undefined;
        }
        var args = arguments;
        var filtered = [];
        for (var i = 0, length = this.length; i < length; i++) {
            var item = this[i];
            if (condition(that.evaluateProperty(item, property), second)) {
                filtered.push(item);
            }
        }
        return filtered;
    };
    //crazy trickery here. mash in the condition function as a property to the filter function, and filter will use it when it runs.
    filter.condition = condition;
    return filter;
};

//reducer function
//applies a function to an array that crawls over each element of the array and ends up producing a single value
//e.g. summing up numbers
//    may need to trick in the data as the prototype will mess up 'this'
//    nope. same reason as above
RqlArray.prototype.reducer = function (func) {
    return function (property) {
        var result = this[0];
        if (property) {
            //apply the reducer function along the array, using the value of the given property
            result = result && result[property];
            for (var i = 1, l = this.length; i < l; i++) {
                result = func(result, this[i][property]);
            }
        } else {
            //apply the reducer function along the array, using the value in the array
            for (var i = 1, l = this.length; i < l; i++) {
                result = func(result, this[i]);
            }
        }
        return result;
    }
};

//evaluateProperty function
RqlArray.prototype.evaluateProperty = function (object, property) {
    if (property instanceof Array) {
        this.each(property, function (part) {
            object = object[decodeURIComponent(part)];
        });
        return object;
    } else if (typeof property == "undefined") {
        return object;
    } else {
        return object[decodeURIComponent(property)];
    }
};

//missingOperator function
RqlArray.prototype.missingOperator = function (operator) {
    throw new Error("Operator " + operator + " is not defined");
};

//it appears this is never used. would explain why there are variables that are not defined (e.g. 'term')
/*
RqlArray.prototype.conditionEvaluator = function(condition){
	var jsOperator = this.jsOperatorMap[term.name];
	if(jsOperator){
		js += "(function(item){return item." + term[0] + jsOperator + "parameters[" + (index -1) + "][1];});";
	}
	else{
		js += "operators['" + term.name + "']";
	}
	return eval(js);
};
*/

//executeQuery function
//the main grinder to execute a query against a json array
//query = the query string
//options = option object
//          .operators - an object containing additional operators that can be used by the query engine
//          .parameters - an array of values to be mapped against $# placeholders.
//                        e.g. ['abc'] would insert 'abc' wherever $1 is written in the query
//target = the array to run the query against
//returns: array of elements that satisfied the query
RqlArray.prototype.executeQuery = function (query, options, target) {
    options = options || {};

    var that = this;

    //parse the query string
    var parser = new RqlParser();
    query = parser.parse(query, options.parameters);

    //generate a class T that has all the operators
    function T() { }
    T.prototype = this.operators;
    var operators = new T;
    // inherit any extra operators from options
    for (var i in options.operators) {
        operators[i] = options.operators[i];
    }
    //crafty function to call operators.
    //will be called by javascript constructed in a string and run via eval
    function op(name) {
        return operators[name] || that.missingOperator(name);
        /*
		if (operators[name]) {
			operators[name].targetData = target;
			return operators[name];
		} else {
			return that.missingOperator(name);
		}
		*/
    }
    //var parameters = options.parameters || [];
    //var js = "";

    //this converts the query into a javascript function (in string form) that will execute the query against the array
    //value = the query (after parsing)
    function queryToJS(value) {
        if (value && typeof value === "object") {
            //query is a query object

            if (value instanceof Array) {
                //call recursively on all elements of the array
                return '[' + that.each(value, function (value, emit) {
                    emit(queryToJS(value));
                }) + ']';
            } else {
                var jsOperator = that.jsOperatorMap[value.name];
                if (jsOperator) {
                    //it's a basic boolean operator (equals / greater / less / etc)
                    //build a path to the javascript property we want to test, testing for each part as we go
                    // item['foo.bar'] ==> (item && item.foo && item.foo.bar && ...)
                    var path = value.args[0];
                    var target = value.args[1];
                    var item;
                    if (typeof target == "undefined") {
                        item = "item";
                        target = path;
                    } else if (path instanceof Array) {
                        item = "item";
                        var escaped = [];
                        for (var i = 0; i < path.length; i++) {
                            escaped.push(that.stringify(path[i]));
                            item += "&&item[" + escaped.join("][") + ']';
                        }
                    } else {
                        item = "item&&item[" + that.stringify(path) + "]";
                    }

                    //make the condition, <path to value> <operator> <target>
                    // e.g. item && item["foo"] === "bar"
                    var condition = item + jsOperator + queryToJS(target);

                    //apply the condition against items in the array, using a filter to weed out those that fail it.  'this' is the array
                    // use native Array.prototype.filter if available
                    if (typeof Array.prototype.filter === 'function') {
                        return "(function(){return this.filter(function(item){return " + condition + "})})";
                        //???return "this.filter(function(item){return " + condition + "})";
                    } else {
                        return "(function(){var filtered = []; for(var i = 0, length = this.length; i < length; i++){var item = this[i];if(" + condition + "){filtered.push(item);}} return filtered;})";
                    }
                } else {
                    //date case
                    if (value instanceof Date) {
                        return value.valueOf();
                    }
                    //otherwise its a fancy operator function (see RqlArray.operators above)
                    //apply the operator using the op function (declared above)
                    return "(function(){return op('" + value.name + "').call(this" +
						(value && value.args && value.args.length > 0 ? (", " + that.each(value.args, function (value, emit) {
						    emit(queryToJS(value));
						}).join(",")) : "") +
						")})";
                }
            }
        } else {
            //query is not an object. return the value
            return typeof value === "string" ? that.stringify(value) : value;
        }
    }
    //generate the query function in string form, then turn it into a real function using eval
    var evaluator = eval("(1&&function(target){return " + queryToJS(query) + ".call(target);})");
    //apply the query function & return results
    return target ? evaluator(target) : evaluator;
};