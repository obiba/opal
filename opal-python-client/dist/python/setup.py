#!/usr/bin/env python

from ez_setup import use_setuptools
use_setuptools()
from distutils.core import setup

VERSION = '@project.version@'
NAME = '@project.name@'
PACKAGE_NAME = 'opal-python-client'
AUTHOR = 'OBiBa'
AUTHOR_EMAIL = 'OBiBa <info@obiba.org>'
MAINTAINER = 'OBiBa'
MAINTAINER_EMAIL = 'OBiBa <info@obiba.org>'
LICENSE = 'GPL-3'
PLATFORMS = "Any"
URL = 'http://www.obiba.org'
DOWNLOAD_URL = '@project.download.url@'
DESCRIPTION = '@project.description@'
DESCRIPTION_LOG = """Data integration Web application for biobanks by OBiBa. Opal is
    OBiBa's core database application for biobanks. Participant data, once
    collected from any data source, must be integrated and stored in a central
    data repository under a uniform model. Opal is such a central repository.
    It can import, process, validate, query, analyze, report, and export data.
    Opal is typically used in a research center to analyze the data acquired at
    assessment centres. Its ultimate purpose is to achieve seamless
    data-sharing among biobanks.
"""
PACKAGES = ['opal', 'opal.protobuf']
PACKAGES_DIR = {'opal': 'bin/opal'}
SCRIPTS = ['bin/scripts/opal']
INSTALL_REQUIRES = ['protobuf >= 2.4', 'pycurl']

setup(
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
    platforms=PLATFORMS,
    packages=PACKAGES,
    package_dir=PACKAGES_DIR,
    scripts=SCRIPTS,
    install_requires=INSTALL_REQUIRES
)
