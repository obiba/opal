"""
Opal data.
"""

import sys
import opal.core
import opal.protobuf.Opal_pb2


def add_arguments(parser):
    """
    Add data command specific options
    """
    parser.add_argument('--name', '-n', required=False, help='User name.')
    parser.add_argument('--upassword', '-upa', required=False, help='User password.')
    parser.add_argument('--disabled', '-di', action='store_true', required=False,
                        help='Disable user account (if ommited the user is enabled by default).')
    parser.add_argument('--groups', '-g', nargs='+', required=False, help='User groups')

    parser.add_argument('--fetch', '-fe', action='store_true', required=False,
                        help='Fetch one or multiple user(s).')
    parser.add_argument('--add', '-a', action='store_true', help='Add a user.')
    parser.add_argument('--update', '-ud', action='store_true', required=False, help='Update a user.')
    parser.add_argument('--delete', '-de', action='store_true', required=False,
                        help='Delete a user.')
    parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')


def do_ws(args):
    """
    Build the web service resource path
    """
    if args.name and args.fetch:
        ws = "/user/" + args.name
    else:
        ws = "/users"

    return ws


def do_command(args):
    """
    Execute group command
    """
    # Build and send request
    try:
        request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()
        request.fail_on_error()

        if args.verbose:
            request.verbose()

        if args.fetch:
            # send request
            response = request.get().resource(do_ws(args)).send()
        elif args.add:
            if not args.name:
                raise Exception('A user name is required.')
            if not args.upassword:
                raise Exception('A user password is required.')

            # create user
            user = opal.protobuf.Opal_pb2.UserDto()
            user.name = args.name
            user.password = args.upassword

            if args.disabled:
                user.enabled = False

            if args.groups:
                user.groups.extend(args.groups)

            request.fail_on_error().accept_json().content_type_protobuf()
            response = request.post().resource("/users").content(user.SerializeToString()).send()
        elif args.update:
            if not args.name:
                raise Exception('A user name is required.')

            user = opal.protobuf.Opal_pb2.UserDto()
            user.name = args.name

            if args.upassword:
                user.password = args.upassword
            if args.disabled:
                user.enabled = False
            if args.groups:
                user.groups.extend(args.groups)

            request.fail_on_error().accept_json().content_type_protobuf()
            response = request.put().resource("/user/" + args.name).content(user.SerializeToString()).send()
        elif args.delete:
            if not args.name:
                raise Exception('A user name is required.')

            response = request.delete().resource("/user/" + args.name).send()

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
