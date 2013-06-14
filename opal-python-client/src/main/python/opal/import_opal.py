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
    parser.add_argument('--ropal', '-ro', required=True, help='Remote Opal server base url')
    parser.add_argument('--ruser', '-ru', required=True, help='Remote User name')
    parser.add_argument('--rpassword', '-rp', required=True, help='Remote User password')
    parser.add_argument('--rdatasource', '-rd', required=True, help='Remote datasource name')
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
        client = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args))
        importer = opal.io.OpalImporter.build(client=client, destination=args.destination, tables=args.tables,
                                              incremental=args.incremental, unit=args.unit, verbose=args.verbose)
        # print result
        extension_factory = OpalExtensionFactory(ropal=args.ropal, rdatasource=args.rdatasource, ruser=args.ruser,
                                                 rpassword=args.rpassword)
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
    def __init__(self, ropal, rdatasource, ruser, rpassword):
        self.ropal = ropal
        self.rdatasource = rdatasource
        self.ruser = ruser
        self.rpassword = rpassword

    def add(self, factory):
        """
        Add specific datasource factory extension
        """
        rest_factory = factory.Extensions[opal.protobuf.Magma_pb2.RestDatasourceFactoryDto.params]
        rest_factory.url = self.ropal
        rest_factory.username = self.ruser
        rest_factory.password = self.rpassword
        rest_factory.remoteDatasource = self.rdatasource


