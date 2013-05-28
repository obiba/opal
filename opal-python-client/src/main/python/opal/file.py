"""
Opal file management.
"""

import sys
import pycurl
import opal.core


class OpalFile:
    """
    File on Opal file system
    """

    def __init__(self, path):
        self.path = path

    def get_meta_ws(self):
        return '/files/meta' + self.path

    def get_ws(self):
        return '/files' + self.path


def add_arguments(parser):
    """
    Add file command specific options
    """
    parser.add_argument('path', help='File path in Opal file system.')
    parser.add_argument('--download', '-dl', action='store_true', help='Download file, or folder (as a zip file).')
    parser.add_argument('--upload', '-up', required=False, help='Upload a local file to a folder in Opal file system.')
    parser.add_argument('--delete', '-dt', action='store_true', help='Delete a file on Opal file system.')
    parser.add_argument('--force', '-f', action='store_true', help='Skip confirmation.')
    parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')


def do_command(args):
    """
    Execute file command
    """
    # Build and send request
    try:
        request = opal.core.OpalClient(args).new_request()
        request.fail_on_error().accept_json()

        if args.verbose:
            request.verbose()

        # build opal file
        file = OpalFile(args.path)

        # send request
        if args.download:
            response = request.get().resource(file.get_ws()).send()
        elif args.upload:
            #boundary = 'OpalPythonClient'
            #request.post().content_type('multipart/form-data; boundary=' + boundary).accept('text/html')
            #content = '--' + boundary + '\n'
            #content = content + 'Content-Disposition: form-data; name="fileToUpload"; filename="'+args.upload+'"\n\n'
            #content = content + open(args.upload,'rb').read()
            #content = content + '\n--' + boundary
            #request.content(content)
            request.content_upload(args.upload).accept('text/html').content_type('multipart/form-data')
            response = request.post().resource(file.get_ws()).send()
        elif args.delete:
            # confirm
            if args.force:
                response = request.delete().resource(file.get_ws()).send()
            else:
                print 'Delete the file "' + args.path + '"? [y/N]: ',
                confirmed = sys.stdin.readline().rstrip().strip()
                if confirmed == 'y':
                    response = request.delete().resource(file.get_ws()).send()
                else:
                    print 'Aborted.'
                    sys.exit(0)
        else:
            response = request.get().resource(file.get_meta_ws()).send()

        # format response
        res = response.content
        if args.json and not args.download and not args.upload:
            res = response.pretty_json()

        # output to stdout
        print response
        print res
    except Exception, e:
        print >> sys.stderr, e
        sys.exit(2)
    except pycurl.error, error:
        print response
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)