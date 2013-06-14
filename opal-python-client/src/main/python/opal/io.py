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

    class ExtensionFactoryInterface:
        def add(self, factory):
            raise Exception("ExtensionFactoryInterface.add() method must be implemented by a concrete class.")

    @classmethod
    def build(cls, client, destination, tables=None, incremental=None, unit=None, verbose=None):
        setattr(cls, 'client', client)
        setattr(cls, 'destination', destination)
        setattr(cls, 'tables', tables)
        setattr(cls, 'incremental', incremental)
        setattr(cls, 'unit', unit)
        setattr(cls, 'verbose', verbose)
        return cls()

    def submit(self, extension_factory):
        """
        Build a specific transient datasource, using extension_factory, and submit import job.
        """
        transient = self.__create_transient_datasource(extension_factory)

        # submit data import job
        request = self.client.new_request()
        request.fail_on_error().accept_json().content_type_protobuf()

        if self.verbose:
            request.verbose()

        # import options
        options = opal.protobuf.Commands_pb2.ImportCommandOptionsDto()
        options.destination = self.destination
        # tables must be the ones of the transient
        tables2import = transient.table
        if self.tables:
            def f(t): return any(t in s for s in transient.table)

            tables2import = filter(f, self.tables)

        def table_fullname(t):
            return transient.name + '.' + t

        options.tables.extend(map(table_fullname, tables2import))
        if self.verbose:
            print "** Import options:"
            print options
            print "**"

        uri = opal.core.UriBuilder(['datasource', self.destination, 'commands', '_import']).build()
        response = request.post().resource(uri).content(options.SerializeToString()).send()

        # get job status
        job_resource = re.sub(r'http.*\/ws', r'', response.headers['Location'])
        request = self.client.new_request()
        request.fail_on_error().accept_json()
        return request.get().resource(job_resource).send()

    def __create_transient_datasource(self, extension_factory):
        """
        Create a transient datasource
        """
        request = self.client.new_request()
        request.fail_on_error().accept_protobuf().content_type_protobuf()

        if self.verbose:
            request.verbose()

        # build transient datasource factory
        factory = opal.protobuf.Magma_pb2.DatasourceFactoryDto()
        if self.incremental:
            factory.incrementalConfig.incremental = True
            factory.incrementalConfig.incrementalDestinationName = self.destination
        if self.unit:
            factory.unitConfig.unit = self.unit
            factory.unitConfig.allowIdentifierGeneration = False
            factory.unitConfig.ignoreUnknownIdentifier = False

        extension_factory.add(factory)

        if self.verbose:
            print "** Datasource factory:"
            print factory
            print "**"

        # send request and parse response as a datasource
        response = request.post().resource('/transient-datasources').content(factory.SerializeToString()).send()
        transient = opal.protobuf.Magma_pb2.DatasourceDto()
        transient.ParseFromString(response.content)

        if self.verbose:
            print "** Transient datasource:"
            print transient
            print "**"
        return transient

    def compare_datasource(self, transient):
        # Compare datasources : /datasource/<transient_name>/compare/<ds_name>
        uri = opal.core.UriBuilder(['datasource',
                                    transient.name.encode('ascii', 'ignore'),
                                    'compare', self.destination]).build()
        request = self.client.new_request()
        request.fail_on_error().accept_protobuf().content_type_protobuf()
        if self.verbose:
            request.verbose()
        response = request.get().resource(uri).send()
        compare = opal.protobuf.Magma_pb2.DatasourceCompareDto()
        compare.ParseFromString(response.content)
        for i in compare.tableComparisons:
            if i.conflicts:
                all_conflicts = []
                for c in i.conflicts:
                    all_conflicts.append(c.code + "(" + ', '.join(c.arguments) + ")")

                raise Exception("Import conflicts: " + '; '.join(all_conflicts))


class OpalExporter:
    """
    OpalExporter takes care of submitting a import job.
    """

    @classmethod
    def build(cls, client, datasource, tables, output, incremental=None, unit=None, verbose=None):
        setattr(cls, 'client', client)
        setattr(cls, 'datasource', datasource)
        setattr(cls, 'tables', tables)
        setattr(cls, 'output', output)
        setattr(cls, 'incremental', incremental)
        setattr(cls, 'unit', unit)
        setattr(cls, 'verbose', verbose)
        return cls()

    def setClient(self, client):
        self.client = client
        return self

    def submit(self, format):
        # export options
        options = opal.protobuf.Commands_pb2.CopyCommandOptionsDto()
        options.format = format
        options.out = self.output
        options.nonIncremental = not self.incremental
        options.noVariables = False
        if self.tables:
            tables2export = self.tables

            def table_fullname(t): return self.datasource + '.' + t

            options.tables.extend(map(table_fullname, tables2export))
        if self.unit:
            options.unit = self.unit

        if self.verbose:
            print "** Export options:"
            print options
            print "**"

        # submit data import job
        request = self.client.new_request()
        request.fail_on_error().accept_json().content_type_protobuf()

        if self.verbose:
            request.verbose()

        uri = opal.core.UriBuilder(['datasource', self.datasource, 'commands', '_copy']).build()
        return request.post().resource(uri).content(options.SerializeToString()).send()
