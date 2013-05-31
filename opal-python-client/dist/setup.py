#!/usr/bin/env python

from distutils.core import setup
import string
import sys

VERSION = '1.15'
NAME = 'OpalPythonClient'
PACKAGE_NAME = 'opal-python-client'
PACKAGE_ROOT_DIR = '../src/main/python/'
AUTHOR = 'OBiBa'
AUTHOR_EMAIL = 'OBiBa <info@obiba.org>'
MAINTAINER = 'OBiBa'
MAINTAINER_EMAIL = 'OBiBa <info@obiba.org>'
LICENSE = 'GPL-3'
PLATFORMS = "Any"
URL = 'http://www.obiba.org'
DOWNLOAD_URL = "http://download.obiba.org/opal/stable/%s-%s.tar.gz" % (NAME, VERSION)
DESCRIPTION = 'Opal Python Client'
DESCRIPTION_LOG = """Data integration Web application for biobanks by OBiBa. Opal is
    OBiBa's core database application for biobanks. Participant data, once
    collected from any data source, must be integrated and stored in a central
    data repository under a uniform model. Opal is such a central repository.
    It can import, process, validate, query, analyze, report, and export data.
    Opal is typically used in a research center to analyze the data acquired at
    assessment centres. Its ultimate purpose is to achieve seamless
    data-sharing among biobanks.
"""


def scan_argv(s):
    p = None
    i = 1
    while i < len(sys.argv):
        arg = sys.argv[i]
        if string.find(arg, s) == 0:
            p = arg[len(s):]
            assert p, arg
            del sys.argv[i]
        else:
            i += 1
    return p


def get_kw(**kw):
    return kw

setup_args = get_kw(
    name=PACKAGE_NAME,
    version=VERSION,
    author=AUTHOR,
    author_email=AUTHOR_EMAIL,
    maintainer=MAINTAINER,
    maintainer_email=MAINTAINER_EMAIL,
    url=URL,
    license=LICENSE,
    description=DESCRIPTION,
    long_description=DESCRIPTION_LOG,
    platforms=PLATFORMS
)

if __name__ == '__main__':
    # extract  commandline option
    package_name = scan_argv('--pkg-name=')
    package_version = scan_argv('--pkg-version=')
    package_description = scan_argv('--pkg-description=')
    package_download_url = scan_argv('--pkg-download-url=')
    # Set the setup() arguments supplied by commandline
    setup_args['name'] = package_name
    setup_args['version'] = package_version
    setup_args['description'] = package_description
    setup_args['download_url'] = package_download_url
    setup_args['packages'] = ['opal', 'opal.protobuf']
    setup_args['package_dir'] = {'opal': 'bin/opal'}

    apply(setup, (), setup_args)