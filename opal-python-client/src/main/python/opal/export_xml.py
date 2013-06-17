"""
Data export in XML.
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
    parser.add_argument('--output', '-out', required=True, help='Output zip file name that will be exported')
    parser.add_argument('--incremental', '-i', action='store_true', help='Incremental export')
    parser.add_argument('--unit', '-un', required=False, help='Unit name for Participant ID mapping')


def do_command(args):
    """
    Execute import data command
    """
    # Build and send request
    try:
        client = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args))
        exporter = opal.io.OpalExporter.build(client=client, datasource=args.datasource, tables=args.tables,
                                              unit=args.unit, output=args.output, incremental=args.incremental,
                                              verbose=args.verbose)
        # Check output filename extension
        if not (args.output.endswith('.zip')):
            raise Exception('Output must be a zip file.')

        # print result
        response = exporter.submit('xml')

        # output to stdout
        print response.code

    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)