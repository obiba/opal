"""
Opal plugin management.
"""

import sys
import pycurl
import opal.core

def add_arguments(parser):
    """
    Add plugin command specific options
    """

    parser.add_argument('--list', '-ls', action='store_true', help='List the installed plugins.')
    parser.add_argument('--updates', '-lu', action='store_true', help='List the installed plugins that can be updated.')
    parser.add_argument('--available', '-la', action='store_true', help='List the new plugins that could be installed.')
    parser.add_argument('--install', '-in', required=False, help='Install a plugin by providing its name or name:version or a path to a plugin archive file (in Opal file system). If no version is specified, the latest version is installed. Requires system restart to be effective.')
    parser.add_argument('--remove', '-rm', required=False, help='Remove a plugin by providing its name. Requires system restart to be effective.')
    parser.add_argument('--reinstate', '-ri', required=False, help='Reinstate a plugin that was previously removed by providing its name.')
    parser.add_argument('--fetch', '-fe', required=False, help='Get the named plugin description.')
    parser.add_argument('--status', '-su', required=False, help='Get the status of the service associated to the named plugin.')
    parser.add_argument('--start', '-sa', required=False, help='Start the service associated to the named plugin.')
    parser.add_argument('--stop', '-so', required=False, help='Stop the service associated to the named plugin.')
    parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')


def do_command(args):
    """
    Execute plugin command
    """
    # Build and send request
    try:
        request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()
        request.fail_on_error().accept_json()

        if args.verbose:
            request.verbose()

        if args.list:
            response = request.get().resource('/plugins').send()
        elif args.updates:
            response = request.get().resource('/plugins/_updates').send()
        elif args.available:
            response = request.get().resource('/plugins/_available').send()
        elif args.install:
            if args.install.startswith('/'):
                response = request.post().resource('/plugins?file=' + args.install).send()
            else:
                nameVersion = args.install.split(':')
                if len(nameVersion) == 1:
                    response = request.post().resource('/plugins?name=' + nameVersion[0]).send()
                else:
                    response = request.post().resource('/plugins?name=' + nameVersion[0] + '&version=' + nameVersion[1]).send()
        elif args.fetch:
            response = request.get().resource('/plugin/' + args.fetch).send()
        elif args.remove:
            response = request.delete().resource('/plugin/' + args.remove).send()
        elif args.reinstate:
            response = request.put().resource('/plugin/' + args.reinstate).send()
        elif args.status:
            response = request.get().resource('/plugin/' + args.status + '/service').send()
        elif args.start:
            response = request.put().resource('/plugin/' + args.start + '/service').send()
        elif args.stop:
            response = request.delete().resource('/plugin/' + args.stop + '/service').send()

        # format response
        res = response.content
        if args.json:
            res = response.pretty_json()

        # output to stdout
        print res
    except Exception, e:
        print >> sys.stderr, e
        sys.exit(2)
    except pycurl.error, error:
        print response
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)