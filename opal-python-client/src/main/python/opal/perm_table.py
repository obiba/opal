"""
Apply permissions on a set of tables.
"""

import sys
import opal.core
import opal.perm


TABLE_PERMISSIONS = ('TABLE_READ', 'TABLE_VALUES', 'TABLE_EDIT', 'TABLE_VALUES_EDIT', 'TABLE_ALL')
TABLE_PERMISSIONS_ARGS = ('view', 'view-value', 'edit', 'edit-values', 'administrate')
SUBJECT_TYPES = ('USER', 'GROUP')


def add_arguments(parser):
    """
    Add variable command specific options
    """
    opal.perm.add_permission_arguments(parser, 'Permission to apply: view, view-value, edit, edit-values or administrate')
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

def map_permission(permission):
    if permission.lower() not in TABLE_PERMISSIONS_ARGS:
        return None

    return {
        'view': 'TABLE_READ',
        'view-value': 'TABLE_VALUES',
        'edit': 'TABLE_EDIT',
        'edit-values': 'TABLE_VALUES_EDIT',
        'administrate': 'TABLE_ALL'
    }[permission.lower()]


def validate_args(args):
    if not args.add and not args.delete:
        raise Exception("You must specify a permission operation: [--add|-a] or [--delete|-de]")

    if args.add:
        if not args.permission:
            raise Exception("A permission name is required: %s" % ', '.join(TABLE_PERMISSIONS_ARGS))
        if map_permission(args.permission) is None:
            raise Exception("Valid table permissions are: %s " % ', '.join(TABLE_PERMISSIONS_ARGS))

    if args.type.upper() not in SUBJECT_TYPES:
        raise Exception("Valid permission types are (%s) " % ', '.join(SUBJECT_TYPES).lower())


def do_command(args):
    """
    Execute permission command
    """
    # Build and send requests
    try:
        validate_args(args)

        for table in args.tables:
            request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()

            if args.verbose:
                request.verbose()

            # send request
            if args.delete:
                request.delete()
            else:
                request.post()

            try:
                response = request.resource(do_ws(args, table)).send()
            except Exception, e:
                print Exception, e

            # format response
            print response.content

    except Exception, e:
        print e
        sys.exit(2)

    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)