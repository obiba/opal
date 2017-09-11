"""
Opal SPSS data import (Using R).
"""

import sys
import re
import opal.core
import opal.io


def add_arguments(parser):
    """
    Add data command specific options
    """
    parser.add_argument('--path', '-pa', required=True, help='SPSS file to import on the Opal filesystem (from the Opal filesystem).')
    parser.add_argument('--locale', '-l', required=False, help='SPSS file locale (e.g. fr, en...).')
    parser.add_argument('--type', '-ty', required=False, help='Entity type (e.g. Participant)')
    parser.add_argument('--idVariable', '-iv', required=False, help='SPSS variable that provides the entity ID. If not specified, first variable values are considered to be the entity identifiers.')

    # non specific import arguments
    opal.io.add_import_arguments(parser)


def do_command(args):
    """
    Execute import data command
    """
    # Build and send request
    try:
        # Check input filename extension
        if not (args.path.endswith('.sav')):
            raise Exception('Input must be a SPSS file (.sav).')

        client = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args))
        importer = opal.io.OpalImporter.build(client=client, destination=args.destination, tables=args.tables,
                                              incremental=args.incremental, limit=args.limit, identifiers=args.identifiers,
                                              policy=args.policy, verbose=args.verbose)
        # print result
        extension_factory = OpalExtensionFactory(path=args.path,
                                                 locale=args.locale, entityType=args.type, idVariable=args.idVariable)

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
    def __init__(self, path, locale, entityType, idVariable):
        self.path = path
        self.locale = locale
        self.entityType = entityType
        self.idVariable = idVariable

    def add(self, factory):
        """
        Add specific datasource factory extension
        """
        factory = factory.Extensions[opal.protobuf.Magma_pb2.RHavenDatasourceFactoryDto.params]
        factory.file = self.path

        if self.locale:
            factory.locale = self.locale

        if self.entityType:
            factory.entityType = self.entityType

        if self.idVariable:
            factory.idVariable = self.idVariable