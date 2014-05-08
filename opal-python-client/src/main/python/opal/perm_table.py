"""
Apply permissions on a set of tables.
"""

import sys
import opal.core
import opal.perm


PERMISSIONS = {
    'view': 'TABLE_READ',
    'view-value': 'TABLE_VALUES',
    'edit': 'TABLE_EDIT',
    'edit-values': 'TABLE_VALUES_EDIT',
    'administrate': 'TABLE_ALL'
}


def add_arguments(parser):
    """
    Add command specific options
    """
    opal.perm.add_permission_arguments(parser, PERMISSIONS.keys())
    parser.add_argument('--project', '-pr', required=True, help='Project name to which the tables belong')
    parser.add_argument('--tables', '-t', nargs='+', required=False,
                        help='List of table names on which the permission is to be set (default is all)')


def do_ws(args, table):
    """
    Build the web service resource path
    """

    if args.add:
        return opal.core.UriBuilder(['project', args.project, 'permissions', 'table', table]) \
            .query('type', args.type.upper()) \
            .query('permission', map_permission(args.permission)) \
            .query('principal', args.subject) \
            .build()

    if args.delete:
        return opal.core.UriBuilder(['project', args.project, 'permissions', 'table', table]) \
            .query('type', args.type.upper()) \
            .query('principal', args.subject) \
            .build()


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
    Execute permission command
    """
    # Build and send requests
    try:
        opal.perm.validate_args(args, PERMISSIONS)

        tables = args.tables
        if not tables:
            tables = retrieve_datasource_tables(args)

        for table in tables:
            request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()

            if args.verbose:
                request.verbose()

            # send request
            if args.delete:
                request.delete()
            else:
                request.post()

            try:
                response = request.resource(
                    opal.perm.do_ws(args, ['project', args.project, 'permissions', 'table', table], PERMISSIONS)).send()
            except Exception, e:
                print Exception, e

            # format response
            if response.code != 200:
                print response.content

    except Exception, e:
        print e
        sys.exit(2)

    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)