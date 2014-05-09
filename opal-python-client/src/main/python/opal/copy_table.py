"""
Data copy.
"""

import sys
import opal.core
import opal.io


def add_arguments(parser):
    """
    Add data command specific options
    """
    parser.add_argument('--datasource', '-d', required=True, help='Datasource/project name of the table to be copied')
    parser.add_argument('--table', '-t', required=True, help='The name of the table to be copied')
    parser.add_argument('--destination', '-de', required=True, help='Destination datasource/project name')
    parser.add_argument('--name', '-na', required=True, help='New table name (required if source and destination are the same)')
    parser.add_argument('--incremental', '-i', action='store_true', help='Incremental copy')
    parser.add_argument('--nulls', '-nu', action='store_true', help='Copy the null values')
    parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')


def do_command(args):
    """
    Execute import data command
    """
    # Build and send request
    try:
        client = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args))
        copier = opal.io.OpalCopier.build(client=client, datasource=args.datasource, table=args.table,
                                            destination=args.destination, name=args.name,
                                            incremental=args.incremental, nulls=args.nulls,
                                            verbose=args.verbose)
        # Check output filename extension
        #if not (args.output.endswith('.zip')):
        #    raise Exception('Output must be a zip file.')

        # print result
        response = copier.submit()

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