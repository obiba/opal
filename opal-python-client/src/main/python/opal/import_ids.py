"""
Import system identifiers
"""

import sys
import opal.core


def add_arguments(parser):
    """
    Add import_ids command specific options
    """
    parser.add_argument('--type', '-t', required=True, help='Entity type (e.g. Participant).')

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

        request.content_type_text_plain()
        print 'Enter identifiers (one identifier per line, Ctrl-D to end input):'
        request.content(sys.stdin.read())

        # send request
        uri = opal.core.UriBuilder(['identifiers', 'mappings', 'entities', '_import']).query('type', args.type).build()
        request.post().resource(uri).send()
    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)