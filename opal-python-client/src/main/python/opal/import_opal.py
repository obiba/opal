"""
Opal data import.
"""

import sys
import re
import opal.core
import opal.protobuf.Magma_pb2
import opal.protobuf.Commands_pb2

def add_arguments(parser):
  """
  Add data command specific options
  """
  #parser.add_argument('name', help='Fully qualified name of a table or a variable, for instance: opal-data.questionnaire or opal-data.questionnaire:Q1.')
  parser.add_argument('--ropal', '-ro', required=True, help='Remote Opal server base url')
  parser.add_argument('--ruser', '-ru', required=True, help='Remote User name')
  parser.add_argument('--rpassword', '-rp', required=True, help='Remote User password')
  parser.add_argument('--rdatasource', '-rd', required=True, help='Remote datasource name')
  parser.add_argument('--destination', '-d', required=True, help='Destination datasource name')
  parser.add_argument('--tables', '-t', nargs='+', required=False, help='The list of tables to be imported (defaults to all)')
  parser.add_argument('--incremental', '-i', action='store_true', help='Incremental import')
  parser.add_argument('--unit', '-un', required=False, help='Unit name for Participant ID mapping')
  parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')

def create_transient_datasource(args):
  """
  Create a transient datasource
  """
  request = opal.core.OpalClient(args).new_request()
  request.fail_on_error().accept_protobuf().content_type_protobuf()

  if args.verbose:
    request.verbose()

  # build transient datasource factory
  factory = opal.protobuf.Magma_pb2.DatasourceFactoryDto()
  if args.incremental:
    factory.incrementalConfig.incremental = True
    factory.incrementalConfig.incrementalDestinationName = args.destination
  if args.unit:
    factory.unitConfig.unit = args.unit
    factory.unitConfig.allowIdentifierGeneration = False
    factory.unitConfig.ignoreUnknownIdentifier = False
  add_datasource_factory_extension(args, factory)
  if args.verbose:
    print "** Datasource factory:"
    print factory
    print "**"
  
  # send request and parse response as a datasource
  response = request.post().resource('/transient-datasources').content(factory.SerializeToString()).send()
  transient = opal.protobuf.Magma_pb2.DatasourceDto()
  transient.ParseFromString(response.content)

  if args.verbose:
    print "** Transient datasource:"
    print transient
    print "**"
  return transient

def add_datasource_factory_extension(args, factory):
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

    transient = create_transient_datasource(args)

    # submit data import job
    request = opal.core.OpalClient(args).new_request()
    request.fail_on_error().accept_json().content_type_protobuf()

    if args.verbose:
      request.verbose()

    # import options
    options = opal.protobuf.Commands_pb2.ImportCommandOptionsDto()
    options.destination = args.destination
    # tables must be the ones of the transient
    tables2import = transient.table
    if args.tables:
      def f(t): return any(t in s for s in transient.table)
      tables2import = filter(f,args.tables)
    def table_fullname(t): return transient.name + '.' + t
    options.tables.extend(map(table_fullname,tables2import))
    if args.verbose:
      print "** Import options:"
      print options
      print "**"

    uri = opal.core.UriBuilder(['datasource', args.destination, 'commands', '_import']).build()
    response = request.post().resource(uri).content(options.SerializeToString()).send()

    # get job status
    job_resource = re.sub(r'http.*\/ws',r'', response.headers['Location'])
    request = opal.core.OpalClient(args).new_request()
    request.fail_on_error().accept_json()
    response = request.get().resource(job_resource).send()

    # format response    
    res = response.content
    if args.json:
      res = response.pretty_json()

    # output to stdout
    print res
  except Exception,e :
    print e
    sys.exit(2)
  except pycurl.error, error:
    errno, errstr = error
    print >> sys.stderr, 'An error occurred: ', errstr
    sys.exit(2)