"""
Apply permissions on a project.
"""

import sys
import opal.core
import opal.perm

PERMISSIONS = {
    'administrate': 'PROJECT_ALL'
}

def add_arguments(parser):
    """
    Add command specific options
    """
    opal.perm.add_permission_arguments(parser, PERMISSIONS.keys())
    parser.add_argument('--project', '-pr', required=True, help='Project name')

def do_command(args):
    """
    Execute permission command
    """
    # Build and send requests
    try:
        opal.perm.validate_args(args, PERMISSIONS)

        request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()

        if args.verbose:
            request.verbose()

        # send request
        if args.delete:
            request.delete()
        else:
            request.post()

        try:
            response = request.resource(opal.perm.do_ws(args, ['project', args.project, 'permissions', 'project'], PERMISSIONS)).send()
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