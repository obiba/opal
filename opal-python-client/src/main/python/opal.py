#! /usr/bin/env python
#
import opal.rest
import opal.dictionary
import opal.data
import opal.file
import opal.entity
import opal.import_opal
import opal.export_xml
import argparse

def add_opal_arguments(parser):
  """
  Add Opal access arguments
  """
  parser.add_argument('--opal', '-o', required=False, help='Opal server base url')
  parser.add_argument('--user', '-u', required=False, help='User name')
  parser.add_argument('--password', '-p', required=False, help='User password')
  parser.add_argument('--verbose', '-v', action='store_true', help='Verbose output')

def add_subcommand(name,help,add_args_func,default_func):
  """
  Make a sub-parser, add default arguments to it, add sub-command arguments and set the sub-command callback function.
  """
  subparser = subparsers.add_parser(name, help=help)
  add_opal_arguments(subparser)
  add_args_func(subparser)
  subparser.set_defaults(func=default_func)
  

# Parse arguments
parser = argparse.ArgumentParser(description='Opal command line.')
subparsers = parser.add_subparsers(title='sub-commands', help='Available sub-commands. Use --help option on the sub-command for more details.')

# Add subcommands
add_subcommand('dict', 'Query for data dictionary.', opal.dictionary.add_arguments,opal.dictionary.do_command)
add_subcommand('data', 'Query for data.', opal.data.add_arguments,opal.data.do_command)
add_subcommand('entity', 'Query for entities (Participant, etc.).', opal.entity.add_arguments,opal.entity.do_command)
add_subcommand('file', 'Manage Opal file system.', opal.file.add_arguments,opal.file.do_command)
add_subcommand('import-opal', 'Import data from a remote Opal server.', opal.import_opal.add_arguments,opal.import_opal.do_command)
add_subcommand('export-xml', 'Export data to a zip of Opal XML files.', opal.export_xml.add_arguments,opal.export_xml.do_command)
add_subcommand('rest', 'Request directly the Opal REST API, for advanced users.', opal.rest.add_arguments,opal.rest.do_command)

# Execute selected command
args = parser.parse_args()
args.func(args)
