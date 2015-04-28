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
    parser.add_argument('--path', '-pa', required=True, help='CSV file to import on the Opal filesystem '
                                                             '(from the Opal filesystem).')
    parser.add_argument('--characterSet', '-c', required=False, help='Character set.')
    parser.add_argument('--separator', '-s', required=False, help='Field separator.')
    parser.add_argument('--quote', '-q', required=False, help='Quotation mark character.')
    parser.add_argument('--firstRow', '-f', type=int, required=False, help='From row.')
    parser.add_argument('--type', '-ty', required=True, help='Entity type (e.g. Participant)')

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
                                              incremental=args.incremental, limit=args.limit,identifiers=args.identifiers,
                                              policy=args.policy, verbose=args.verbose,
                                              create_variables=args.create_variables)

        # print result
        extension_factory = OpalExtensionFactory(characterSet=args.characterSet, separator=args.separator,
                                                 quote=args.quote,
                                                 firstRow=args.firstRow, path=args.path, type=args.type,
                                                 tables=args.tables,
                                                 destination=args.destination)

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
    def __init__(self, characterSet, separator, quote, firstRow, path, type, tables, destination):
        self.characterSet = characterSet
        self.separator = separator
        self.quote = quote
        self.firstRow = firstRow
        self.path = path
        self.type = type
        self.tables = tables
        self.destination = destination


    def add(self, factory):
        """
        Add specific datasource factory extension
        """
        csv_factory = factory.Extensions[opal.protobuf.Magma_pb2.CsvDatasourceFactoryDto.params]

        if self.characterSet:
            csv_factory.characterSet = self.characterSet

        if self.separator:
            csv_factory.separator = self.separator

        if self.quote:
            csv_factory.quote = self.quote

        if self.firstRow:
            csv_factory.firstRow = self.firstRow

        table = csv_factory.tables.add()
        table.data = self.path
        table.entityType = self.type

        if self.tables:
            table.name = self.tables[0]
        else:
            # Take filename as the table name
            name = self.path.split("/")

            index = name[-1].find('.csv')
            if index > 0:
                table.name = name[-1][:-index]
            else:
                table.name = name[-1]

        table.refTable = self.destination + "." + table.name