"""
Data export in XML.
"""

import sys
import re
import opal.core
import opal.io

def add_arguments(parser):
  """
  Add data command specific options
  """
  parser.add_argument('--datasource', '-d', required=True, help='Datasource name')
  parser.add_argument('--tables', '-t', nargs='+', required=True, help='The list of tables to be exported')
  parser.add_argument('--output', '-out', required=True, help='Output zip file name that will be exported')
  parser.add_argument('--incremental', '-i', action='store_true', help='Incremental export')
  parser.add_argument('--unit', '-un', required=False, help='Unit name for Participant ID mapping')
  parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')

def do_command(args):
  """
  Execute import data command
  """
  # Build and send request
  try:
    exporter = opal.io.OpalExporter(args)
    # print result
    print exporter.submit('xml')
  except Exception,e :
    print e
    sys.exit(2)
  except pycurl.error, error:
    errno, errstr = error
    print >> sys.stderr, 'An error occurred: ', errstr
    sys.exit(2)