"""
Import some VCF/BCF files.
"""

import sys
import opal.core


def add_arguments(parser):
    """
    Add command specific options
    """
    parser.add_argument('--project', '-pr', required=True, help='Project name into which genotypes data will be imported')
    parser.add_argument('--vcf', '-vcf', nargs='+', required=True,
                        help='List of VCF/BCF (optionally compressed) file paths (in Opal file system)')

def do_command(args):
    """
    Execute delete command
    """
    # Build and send requests
    try:
        request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()
        request.fail_on_error().accept_json().content_type_protobuf()
        if args.verbose:
            request.verbose()

        options = opal.protobuf.Commands_pb2.ImportVCFCommandOptionsDto()
        options.project = args.project
        options.files.extend(args.vcf)

        # send request
        uri = opal.core.UriBuilder(['project', args.project, 'commands', '_import_vcf']).build()
        request.resource(uri).post().content(options.SerializeToString()).send()
    except Exception, e:
        print e
        sys.exit(2)

    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)