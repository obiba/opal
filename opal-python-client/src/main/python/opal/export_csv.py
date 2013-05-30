"""
Data export in CSV.
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
    parser.add_argument('--output', '-out', required=True, help='Output directory name')
    parser.add_argument('--incremental', '-i', action='store_true', help='Incremental export')
    parser.add_argument('--unit', '-un', required=False, help='Unit name for Participant ID mapping')


def do_command(args):
    """
    Execute import data command
    """
    # Build and send request
    try:
        client = opal.core.OpalClient.build(args.opal, args.user, args.password)
        exporter = opal.io.OpalExporter.build(client=client, datasource=args.datasource, tables=args.tables,
                                              unit=args.unit, output=args.output, incremental=args.incremental,
                                              verbose=args.verbose)
        # print result
        response = exporter.submit('csv')

        # output to stdout
        print response.code

    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)