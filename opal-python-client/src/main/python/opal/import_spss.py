"""
Opal SPSS data import.
"""

import sys
import re
import opal.core
import opal.io


def add_arguments(parser):
    """
    Add data command specific options
    """
    parser.add_argument('--path', '-pa', required=True, help='SPSS file to import on the Opal filesystem '
                                                             '(from the Opal filesystem).')
    parser.add_argument('--characterSet', '-c', required=False, help='Character set.')
    parser.add_argument('--locale', '-l', required=False, help='SPSS file locale (e.g. fr, en...).')
    parser.add_argument('--type', '-ty', required=False, help='Entity type (e.g. Participant)')

    # non specific import arguments
    parser.add_argument('--destination', '-d', required=True, help='Destination datasource name')
    parser.add_argument('--tables', '-t', nargs='+', required=False, help='Name of the table.')
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
                                              incremental=args.incremental, unit=args.unit,
                                              verbose=args.verbose)
        # print result
        extension_factory = OpalExtensionFactory(characterSet=args.characterSet, path=args.path,
                                                 locale=args.locale, entityType=args.type)

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
    def __init__(self, characterSet, path, locale, entityType):
        self.characterSet = characterSet
        self.path = path
        self.locale = locale
        self.entityType = entityType


    def add(self, factory):
        """
        Add specific datasource factory extension
        """
        factory = factory.Extensions[opal.protobuf.Magma_pb2.SpssDatasourceFactoryDto.params]
        factory.file = self.path

        if self.characterSet:
            factory.characterSet = self.characterSet

        if self.locale:
            factory.locale = self.locale

        if self.entityType:
            factory.entityType = self.entityType