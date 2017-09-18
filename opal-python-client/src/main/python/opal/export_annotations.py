"""
Opal dictionary annotations extraction.
"""

import json
import opal.core
import sys
import csv
import argparse


def add_arguments(parser):
    """
    Add command specific options
    """
    parser.add_argument('name',
                        help='Fully qualified name of a datasource/project or a table or a variable, for instance: opal-data or opal-data.questionnaire or opal-data.questionnaire:Q1. Wild cards can also be used, for instance: "opal-data.*", etc.')
    parser.add_argument('--output', '-out', help='CSV/TSV file to output (default is stdout)', type=argparse.FileType('w'), default=sys.stdout)
    parser.add_argument('--separator', '-s', required=False, help='Separator char for CSV/TSV format (default is the tabulation character)')
    parser.add_argument('--taxonomies', '-t', nargs='+', required=False, help='The list of taxonomy names of interest (default is any)')


def do_command(args):
    """
    Execute command
    """
    # Build and send request
    try:
        sep = csv_separator(args)
        writer = csv.writer(args.output, delimiter=sep)
        writer.writerow(['project','table','variable','namespace','name','value'])
        handle_item(args, writer, args.name)
    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)


def handle_item(args, writer, name):
    #print 'Handling ' + name
    request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()
    request.fail_on_error().accept_json()

    if args.verbose:
        request.verbose()

    # send request
    resolver = opal.core.MagmaNameResolver(name)
    request.get().resource(resolver.get_ws())
    response = request.send()

    if resolver.is_datasources():
        raise Exception('Wildcard not allowed for datasources/projects')

    res = json.loads(response.content)
    if resolver.is_datasource():
        handle_datasource(args, writer, res)
    if resolver.is_table():
        handle_table(args, writer, res)
    if resolver.is_variables():
        for variable in res:
            handle_variable(args, writer, resolver.datasource, resolver.table, variable)
    if resolver.is_variable():
        handle_variable(args, writer, resolver.datasource, resolver.table, res)

def handle_datasource(args, writer, datasourceObject):
    for table in datasourceObject['table']:
        handle_item(args, writer, datasourceObject['name'] + '.' + table + ':*')


def handle_table(args, writer, tableObject):
    handle_item(args, writer, tableObject['datasourceName'] + '.' + tableObject['name'] + ':*')


def handle_variable(args, writer, datasource, table, variableObject):
    sep = csv_separator(args)
    if 'attributes' in variableObject:
        for attribute in variableObject['attributes']:
            if 'namespace' in attribute and 'locale' not in attribute:
                if not args.taxonomies or attribute['namespace'] in args.taxonomies:
                    writer.writerow([datasource,table,variableObject['name'],attribute['namespace'],attribute['name'],attribute['value']])


def csv_separator(args):
    return args.separator if args.separator else '\t'