"""
Import identifiers mapping
"""

import sys
import opal.core


def add_arguments(parser):
    """
    Add import_idsmap command specific options
    """
    parser.add_argument('--type', '-t', required=True, help='Entity type (e.g. Participant).')
    parser.add_argument('--map', '-m', required=True, help='Mapping name.')
    parser.add_argument('--separator', '-s', required=False, help='Field separator (default is ,).')

def do_command(args):
    """
    Execute import command
    """
    # Build and send request
    try:
        request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()
        request.fail_on_error()

        if args.verbose:
            request.verbose()

        request.content_type("text/plain")
        print 'Enter identifiers (one identifiers mapping per line, Ctrl-D to end input):'
        request.content(sys.stdin.read())

        # send request
        builder = opal.core.UriBuilder(['identifiers', 'mapping', args.map, '_import']).query('type', args.type)
        if args.separator:
            builder.query('separator', args.separator)
        uri = builder.build()
        request.post().resource(uri).send()
    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)