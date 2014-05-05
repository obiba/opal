"""
Apply permissions on a set of tables.
"""

import sys
import opal.core


def add_arguments(parser):
    """
    Add variable command specific options
    """
    parser.add_argument('--permission', '-pe', required=True, help='Permission to apply: view, view-value, edit, edit-values or administrate')
    parser.add_argument('--subject', '-s', required=True, help='Subject name to which the permission will be granted')
    parser.add_argument('--type', '-ty', required=False, help='Subject type: user or group (default is user)')
    parser.add_argument('--datasource', '-d', required=True, help='Datasource/project name to which the tables belong')
    parser.add_argument('--tables', '-t', nargs='+', required=False, help='List of table names on which the permission is to be set (default is all)')


def do_command(args):
    """
    Execute variable command
    """
    # Build and send request
    try:

        request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()
        request.fail_on_error().accept_json()

        if args.verbose:
            request.verbose()

        # send request
        request.get().resource(opal.core.MagmaNameResolver(args.name).get_ws())
        response = request.send()
    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)