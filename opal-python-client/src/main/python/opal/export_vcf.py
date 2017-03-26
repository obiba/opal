"""
Export some VCF/BCF files.
"""

import sys
import opal.core


def add_arguments(parser):
    """
    Add command specific options
    """
    parser.add_argument('--project', '-pr', required=True, help='Project name from which genotypes data will be exported')
    parser.add_argument('--vcf', '-vcf', nargs='+', required=True, help='List of VCF/BCF file names')
    parser.add_argument('--destination', '-d', required=True, help='Destination folder (in Opal file system)')
    parser.add_argument('--filter-table', '-f', required=False, help='Participant table name to be used to filter the samples by participant ID (only relevant if there is a sample-participant mapping defined)')
    parser.add_argument('--no-case-controls', '-nocc', action='store_true', help='Do not include case control samples (only relevant if there is a sample-participant mapping defined)')

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

        options = opal.protobuf.Commands_pb2.ExportVCFCommandOptionsDto()
        options.project = args.project
        options.names.extend(args.vcf)
        options.destination = args.destination
        options.caseControl = not args.no_case_controls
        if args.filter_table:
            options.table = args.filter_table

        # send request
        uri = opal.core.UriBuilder(['project', args.project, 'commands', '_export_vcf']).build()
        request.resource(uri).post().content(options.SerializeToString()).send()
    except Exception, e:
        print e
        sys.exit(2)

    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)