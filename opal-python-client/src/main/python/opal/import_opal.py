"""
Opal data import.
"""

import sys
import re
import opal.core
import opal.importer

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
  parser.add_argument('--tables', '-t', nargs='+', required=False, help='The list of tables to be imported (defaults to all)')
  parser.add_argument('--incremental', '-i', action='store_true', help='Incremental import')
  parser.add_argument('--unit', '-un', required=False, help='Unit name for Participant ID mapping')
  parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')

def add_rest_datasource_factory_extension(args, factory):
  """
  Add specific datasource factory extension
  """
  rest_factory = factory.Extensions[opal.protobuf.Magma_pb2.RestDatasourceFactoryDto.params]
  rest_factory.url = args.ropal
  rest_factory.username = args.ruser
  rest_factory.password = args.rpassword
  rest_factory.remoteDatasource = args.rdatasource

def do_command(args):
  """
  Execute import data command
  """
  # Build and send request
  try:
    importer = opal.importer.OpalImporter(args)
    # print result
    print importer.submit_import(add_rest_datasource_factory_extension)
  except Exception,e :
    print e
    sys.exit(2)
  except pycurl.error, error:
    errno, errstr = error
    print >> sys.stderr, 'An error occurred: ', errstr
    sys.exit(2)