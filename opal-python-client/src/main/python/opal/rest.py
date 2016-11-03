"""
Examples of Opal web services on which GET can be performed:

/datasources
  All datasources

/datasource/xxx
  A datasource

/datasource/xxx/tables
  All tables of a datasource

/datasource/xxx/table/yyy
  A table

/datasource/xxx/table/yyy/variables
  All variables of a table

/datasource/xxx/table/yyy/variable/vvv
  A variable

/datasource/xxx/table/yyy/entities
  All entities of a table

/datasource/xxx/table/yyy/entities?script=sss
  All entities of a table matching a script (see http://wiki.obiba.org/display/OPALDOC/Magma+Javascript+API)

/datasource/xxx/table/yyy/valueSet/zzz
  All values of a entity in a table

/datasource/xxx/table/yyy/valueSet/zzz/variable/vvv
  A variable value of a entity

/datasource/xxx/table/yyy/valueSet/zzz/variable/vvv/value
  Raw variable value of a entity

/datasource/xxx/table/yyy/valueSet/zzz/variable/vvv/value?pos=1
  Raw repeatable variable value of a entity at given position (start at 0)
"""

import sys
import ast
import opal.core


def add_arguments(parser):
    """
    Add REST command specific options
    """
    parser.add_argument('ws', help='Web service path, for instance: /datasource/xxx/table/yyy/variable/vvv')
    parser.add_argument('--method', '-m', required=False,
                        help='HTTP method (default is GET, others are POST, PUT, DELETE, OPTIONS)')
    parser.add_argument('--accept', '-a', required=False, help='Accept header (default is application/json)')
    parser.add_argument('--content-type', '-ct', required=False,
                        help='Content-Type header (default is application/json)')
    parser.add_argument('--headers', '-hs', required=False, help='Custom headers in the form of: { "Key2": "Value2", "Key2": "Value2" }')
    parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')


def do_command(args):
    """
    Execute REST command
    """
    # Build and send request
    try:
        request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()
        request.fail_on_error()

        if args.accept:
            request.accept(args.accept)
        else:
            request.accept_json()

        if args.content_type:
            request.content_type(args.content_type)
            print 'Enter content:'
            request.content(sys.stdin.read())

        if args.headers:
            headers = ast.literal_eval(args.headers)
            for key in headers.keys():
                request.header(key, headers[key])

        if args.verbose:
            request.verbose()

        # send request
        request.method(args.method).resource(args.ws)
        response = request.send()

        # format response
        res = response.content
        if args.json:
            res = response.pretty_json()
        elif args.method in ['OPTIONS']:
            res = response.headers['Allow']

        # output to stdout
        print res
    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)