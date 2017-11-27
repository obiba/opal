"""
Manage a task: show, status, wait, cancel, delete.
"""

import sys
import ast
import json
import time
import opal.core


def add_arguments(parser):
    """
    Add task command specific options
    """
    parser.add_argument('--id', required=False, help='The task ID. If not provided, it will be read from the standard input (from the JSON representation of the task or a plain value).')
    parser.add_argument('--show', '-sh', action='store_true', help='Show JSON representation of the task')
    parser.add_argument('--status', '-st', action='store_true', help='Get the status of the task')
    parser.add_argument('--wait', '-w', action='store_true', help='Wait for the task to complete (successfully or not)')
    parser.add_argument('--cancel', '-c', action='store_true', help='Cancel the task')
    parser.add_argument('--delete', '-d', action='store_true', help='Delete the task')
    parser.add_argument('--json', '-j', action='store_true', help='Pretty JSON formatting of the response')

def new_request(args):
    request = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args)).new_request()
    request.fail_on_error()
    request.accept_json()
    if args.verbose:
        request.verbose()
    return request

def show_task(args):
    task = get_task(args)
    if args.json:
        print json.dumps(task, sort_keys=True, indent=2)
    else:
        print json.dumps(task)

def get_task(args):
    request = new_request(args)
    request.get().resource('/shell/command/' + args.id)
    response = request.send()
    return json.loads(response.content)

def delete_task(args):
    request = new_request(args)
    request.delete().resource('/shell/command/' + args.id).send()

def cancel_task(args):
    request = new_request(args).content_type_text_plain()
    request.content('CANCELED')
    request.put().resource('/shell/command/' + args.id + '/status').send()

def wait_task(args):
    task = get_task(args)
    while task['status'] not in ['SUCCEEDED','CANCELED','FAILED']:
        if 'progress' in task:
            progress = task['progress']
            if 'message' in progress:
                sys.stdout.write('\r\033[K' + str(progress['percent']) + '% ' + progress['message'])
            else:
                sys.stdout.write('\r\033[K' + str(progress['percent']) + '%')
        else:
            sys.stdout.write('.')
        sys.stdout.flush()
        time.sleep(1)
        task = get_task(args)
    print '\r\033[K' + task['status']

def do_command(args):
    """
    Execute task command
    """
    # Build and send request
    try:
        # Extract task identifier from stdin: can be the ID or the task in JSON
        if not args.id:
            id = sys.stdin.read().strip('\n')
            if id.startswith('{'):
                id = str(json.loads(id)['id'])
            args.id = id

        if args.show or not (args.show or args.wait or args.status or args.cancel or args.delete):
            show_task(args)
        if args.wait:
            wait_task(args)
        if args.status:
            print get_task(args)['status']
        if args.cancel:
            cancel_task(args)
        if args.delete:
            delete_task(args)

    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)