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
    parser.add_argument('--project', '-pr', required=True, help='Source project name')
    parser.add_argument('--tables', '-t', nargs='+', required=False,
                        help='List of table names to be copied (default is all)')
    parser.add_argument('--destination', '-d', required=True, help='Destination project name')
    parser.add_argument('--name', '-na', required=False,
                        help='New table name (required if source and destination are the same, ignored if more than one table is to be copied)')
    parser.add_argument('--incremental', '-i', action='store_true', help='Incremental copy')
    parser.add_argument('--nulls', '-nu', action='store_true', help='Copy the null values')
    parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')


def retrieve_datasource_tables(args):
    request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()
    request.fail_on_error()
    if args.verbose:
        request.verbose()
    response = request.get().resource(
        opal.core.UriBuilder(['datasource', args.project, 'tables']).build()).send().as_json()

    tables = []
    for table in response:
        tables.append(str(table[u'name']))

    return tables


def do_command(args):
    """
    Execute copy data command
    """

    tables = args.tables
    if not tables:
        tables = retrieve_datasource_tables(args)

    # Build and send request
    try:
        client = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args))
        copier = opal.io.OpalCopier.build(client=client, datasource=args.project, tables=tables,
                                          destination=args.destination, name=args.name,
                                          incremental=args.incremental, nulls=args.nulls,
                                          verbose=args.verbose)

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