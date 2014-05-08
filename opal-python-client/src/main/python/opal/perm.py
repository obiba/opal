"""
Opal permissions
"""

import re
import opal.core
import opal.protobuf.Magma_pb2
import opal.protobuf.Commands_pb2

def add_permission_arguments(parser, permission_help):
    """
    Add Default Permission arguments
    """
    parser.add_argument('--add', '-a', action='store_true', help='Add a permission')
    parser.add_argument('--delete', '-d', action='store_true', required=False, help='Delete a permission')
    parser.add_argument('--permission', '-pe', help=permission_help)
    parser.add_argument('--subject', '-s', required=True, help='Subject name to which the permission will be granted')
    parser.add_argument('--type', '-ty', required=False, help='Subject type: user or group (default is user)')
