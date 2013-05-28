"""
Opal CSV data import.
"""

import sys
import re
import opal.core
import opal.io


def add_arguments(parser):
    """
    Add data command specific options
    """
    parser.add_argument('--path', '-pa', required=True, help='Local path of the CSV file to import.')
    parser.add_argument('--characterSet', '-c', required=False, help='Character set')
    parser.add_argument('--separator', '-s', required=False, help='Field separator.')
    parser.add_argument('--quote', '-q', required=False, help='Fields are enclosed by quotes.')
    parser.add_argument('--firstRow', '-f', required=False, help='Skip first row as it contains column names.')

    # non specific import arguments
    parser.add_argument('--destination', '-d', required=True, help='Destination datasource name')
    parser.add_argument('--tables', '-t', nargs='+', required=False,
                        help='The list of tables to be imported (defaults to all)')
    parser.add_argument('--incremental', '-i', action='store_true', help='Incremental import')
    parser.add_argument('--unit', '-un', required=False, help='Unit name for Participant ID mapping')
    parser.add_argument('--type', '-ty', required=True, help='Entity type (e.g. Participant)')
    parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')


def add_csv_datasource_factory_extension(args, factory):
    """
    Add specific datasource factory extension
    """
    csv_factory = factory.Extensions[opal.protobuf.Magma_pb2.CsvDatasourceFactoryDto.params]
    # csv_factory.incremental = True

    # if args.characterSet:
    #     csv_factory.characterSet = args.characterSet
    # else:
    #     csv_factory.characterSet = 'utf-8'

    if args.separator:
        csv_factory.separator = args.separator
    else:
        csv_factory.separator = ','
        #
        # if args.quote:
        #     csv_factory.quote = args.quote
        # else:
        #     csv_factory.quote = ''
        #
        # if args.firstRow:
        #     csv_factory.firstRow = args.firstRow
        # else:
        #     csv_factory.firstRow = True


        # name='bundle', full_name='Magma.CsvDatasourceFactoryDto.bundle', index=0,


def do_command(args):
    """
    Execute import data command
    """
    # Build and send request
    try:
        importer = opal.io.OpalImporter(args)
        # print result
        print importer.submit(add_csv_datasource_factory_extension)
    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)