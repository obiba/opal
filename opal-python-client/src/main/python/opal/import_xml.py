"""
Opal Xml import.
"""

import sys
import opal.core
import opal.io


def add_arguments(parser):
    """
    Add data command specific options
    """
    parser.add_argument('--path', '-pa', required=True, help='Zip of XML files to import on the Opal filesystem '
                                                             '(from the Opal filesystem).')
    # non specific import arguments
    opal.io.add_import_arguments(parser)


def do_command(args):
    """
    Execute import data command
    """
    # Build and send request
    try:
        client = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args))
        importer = opal.io.OpalImporter.build(client=client, destination=args.destination, tables=args.tables,
                                              incremental=args.incremental, limit=args.limit, unit=args.unit,
                                              verbose=args.verbose)
        # print result
        extension_factory = OpalExtensionFactory(path=args.path)

        response = importer.submit(extension_factory)

        # format response
        res = response.content
        if args.json:
            res = response.pretty_json()

        # output to stdout
        print res
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
        xml_factory.file = self.path