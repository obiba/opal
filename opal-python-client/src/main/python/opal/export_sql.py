"""
Data export to a SQL database.
"""

import sys
import opal.core
import opal.io


def add_arguments(parser):
    """
    Add data command specific options
    """
    parser.add_argument('--datasource', '-d', required=True, help='Datasource name')
    parser.add_argument('--tables', '-t', nargs='+', required=True, help='The list of tables to be exported')
    parser.add_argument('--output', '-out', required=True, help='Registered database name where tables will be exported')
    parser.add_argument('--incremental', '-i', action='store_true', help='Incremental export')
    parser.add_argument('--identifiers', '-id', required=False, help='Name of the ID mapping')
    parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')


def do_command(args):
    """
    Execute export data command
    """
    # Build and send request
    try:
        client = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args))
        exporter = opal.io.OpalExporter.build(client=client, datasource=args.datasource, tables=args.tables,
                                              identifiers=args.identifiers, output=args.output, incremental=args.incremental,
                                              verbose=args.verbose)

        # print result
        response = exporter.submit('jdbc')

        # format response
        res = response.content
        if args.json:
            res = response.pretty_json()

        # output to stdout
        print res

    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)