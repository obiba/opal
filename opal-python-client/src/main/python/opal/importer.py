"""
Opal data importer
"""

import re
import opal.core
import opal.protobuf.Magma_pb2
import opal.protobuf.Commands_pb2


class OpalImporter:
  """
  OpalImporter takes care of submitting a import job.
  """
  def __init__(self, args):
  	self.args = args

  def submit_import(self, extension_factory):
    """
    Build a specific transient datasource, using extension_factory, and submit import job.
    """
    transient = self.__create_transient_datasource(extension_factory)

    # submit data import job
    request = opal.core.OpalClient(self.args).new_request()
    request.fail_on_error().accept_json().content_type_protobuf()

    if self.args.verbose:
      request.verbose()

    # import options
    options = opal.protobuf.Commands_pb2.ImportCommandOptionsDto()
    options.destination = self.args.destination
    # tables must be the ones of the transient
    tables2import = transient.table
    if self.args.tables:
      def f(t): return any(t in s for s in transient.table)
      tables2import = filter(f,self.args.tables)
    def table_fullname(t): return transient.name + '.' + t
    options.tables.extend(map(table_fullname,tables2import))
    if self.args.verbose:
      print "** Import options:"
      print options
      print "**"

    uri = opal.core.UriBuilder(['datasource', self.args.destination, 'commands', '_import']).build()
    response = request.post().resource(uri).content(options.SerializeToString()).send()

    # get job status
    job_resource = re.sub(r'http.*\/ws',r'', response.headers['Location'])
    request = opal.core.OpalClient(self.args).new_request()
    request.fail_on_error().accept_json()
    response = request.get().resource(job_resource).send()

    # format response    
    res = response.content
    if self.args.json:
      res = response.pretty_json()

    # return result
    return res

  def __create_transient_datasource(self, extension_factory):
  	"""
  	Create a transient datasource
  	"""
  	request = opal.core.OpalClient(self.args).new_request()
  	request.fail_on_error().accept_protobuf().content_type_protobuf()

	if self.args.verbose:
	  request.verbose()

	# build transient datasource factory
	factory = opal.protobuf.Magma_pb2.DatasourceFactoryDto()
	if self.args.incremental:
	  factory.incrementalConfig.incremental = True
	  factory.incrementalConfig.incrementalDestinationName = self.args.destination
	if self.args.unit:
	  factory.unitConfig.unit = self.args.unit
	  factory.unitConfig.allowIdentifierGeneration = False
	  factory.unitConfig.ignoreUnknownIdentifier = False
	self.__add_datasource_factory_extension(extension_factory, factory)
	if self.args.verbose:
	  print "** Datasource factory:"
	  print factory
	  print "**"
	  
	# send request and parse response as a datasource
	response = request.post().resource('/transient-datasources').content(factory.SerializeToString()).send()
	transient = opal.protobuf.Magma_pb2.DatasourceDto()
	transient.ParseFromString(response.content)

	if self.args.verbose:
	  print "** Transient datasource:"
	  print transient
	  print "**"
	return transient

  def __add_datasource_factory_extension(self, extension_factory, factory):
    """
    Add specific datasource factory extension
    """
    extension_factory(self.args, factory)
