"""
Opal data import.
"""

import sys
import opal.core
import opal.io


def add_arguments(parser):
    """
    Add data command specific options
    """
    parser.add_argument('--path', '-pa', required=True, help='CSV file to import on the Opal filesystem '
                                                             '(from the Opal filesystem).')
    # non specific import arguments
    parser.add_argument('--destination', '-d', required=True, help='Destination datasource name')
    parser.add_argument('--tables', '-t', nargs='+', required=False,
                        help='The list of tables to be imported (defaults to all)')
    parser.add_argument('--incremental', '-i', action='store_true', help='Incremental import')
    parser.add_argument('--unit', '-un', required=False, help='Unit name for Participant ID mapping')
    parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')


def do_command(args):
    """
    Execute import data command
    """
    # Build and send request
    try:
        client = opal.core.OpalClient.build(args.opal, args.user, args.password)
        importer = opal.io.OpalImporter.build(client=client, destination=args.destination, tables=args.tables,
                                              incremental=args.incremental, unit=args.unit, json=args.json,
                                              verbose=args.verbose)
        # print result
        extension_factory = OpalExtensionFactory(path=args.path)
        print importer.submit(extension_factory)
    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)


class OpalExtensionFactory(opal.io.OpalImporter.ExtensionFactoryInterface):
    def __init__(self, path):
        self.path = path

    def add(self, factory):
        """
        Add specific datasource factory extension
        """
        xml_factory = factory.Extensions[opal.protobuf.Magma_pb2.FsDatasourceFactoryDto.params]
        xml_factory.url = self.path