"""
Apply permissions on a set of tables.
"""

import sys
import opal.core


TABLE_PERMISSIONS = ('TABLE_READ', 'TABLE_VALUES', 'TABLE_EDIT', 'TABLE_VALUES_EDIT', 'TABLE_ALL')
USER_PERMISSIONS = ('view', 'view-value', 'edit', 'edit-values', 'administrate')
PERMISSION_TYPES = ('USER', 'GROUP')


def add_arguments(parser):
    """
    Add variable command specific options
    """
    parser.add_argument('--add', '-a', action='store_true', help='Add permission.')
    parser.add_argument('--delete', '-d', action='store_true', required=False, help='Delete a user.')
    parser.add_argument('--permission', '-pe',
                        help='Permission to apply: view, view-value, edit, edit-values or administrate')
    parser.add_argument('--subject', '-s', required=True, help='Subject name to which the permission will be granted')
    parser.add_argument('--type', '-ty', required=False, help='Subject type: user or group (default is user)')
    parser.add_argument('--datasource', '-ds', required=True, help='Datasource/project name to which the tables belong')
    parser.add_argument('--tables', '-t', nargs='+', required=False,
                        help='List of table names on which the permission is to be set (default is all)')


def do_ws(args, table):
    """
    Build the web service resource path
    """

    if args.add:
        return opal.core.UriBuilder(['project', args.datasource, 'permissions', 'table', table]) \
            .query('type', args.type.upper()) \
            .query('permission', map_permission(args.permission)) \
            .query('principal', args.subject) \
            .build()

        # return UriBuilder("/project/%s/permissions/table/%s?type=%s&permission=%s&principal=%s" % \
        #        (args.datasource, table, args.type.upper(), map_permission(args.permission), args.subject)).build()
    if args.delete:
        return opal.core.UriBuilder(['project', args.datasource, 'permissions', 'table', table]) \
            .query('type', args.type.upper()) \
            .query('principal', args.subject) \
            .build()

        # return "/project/%s/permissions/table/%s?type=%s&principal=%s" % \
        #        (args.datasource, table, args.type.upper(), args.subject)


def map_permission(permission):
    if permission.lower() not in USER_PERMISSIONS:
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
            raise Exception("A permission name is required: %s" % ', '.join(USER_PERMISSIONS))
        if map_permission(args.permission) is None:
            raise Exception("Valid table permissions are: %s " % ', '.join(USER_PERMISSIONS))

    if args.type.upper() not in PERMISSION_TYPES:
        raise Exception("Valid permission types are (%s) " % ', '.join(PERMISSION_TYPES).lower())


def do_command(args):
    """
    Execute variable command
    """
    # Build and send request
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