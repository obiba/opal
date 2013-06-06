# Makefile responsible for building python and debian packages
#

PYTHON=`which python`
TARGETDIR=$(CURDIR)
DESTDIR=/
VERSION=1.15
DOWNLOAD_URL=http://download.obiba.org/
DESCRIPTION=Opal Python Client
SETUP_ARGS=--pkg-name=$(PACKAGE) --pkg-version=$(VERSION) --pkg-description=DESCRIPTION --pkg-download-url=DOWNLOAD_URL
PROJECT=$(PACKAGE)-$(VERSION)
BUILDIR=$(CURDIR)/debian/$(PROJECT)


all:
	@echo "make source - Create source package"
	@echo "make install - Install on local system"
	@echo "make buildrpm - Generate a rpm package"
	@echo "make builddeb - Generate a deb package"
	@echo "make clean - Get rid of scratch and byte files"

source:
	$(PYTHON) setup.py $(SETUP_ARGS) sdist $(COMPILE)

install:
	$(PYTHON) setup.py install --root $(DESTDIR) $(COMPILE)

buildrpm:
	$(PYTHON) setup.py $(SETUP_ARGS) bdist_rpm --post-install=rpm/postinstall --pre-uninstall=rpm/preuninstall

builddeb:
	@echo "THIS IS THE PACKAGE NAME $(PACKAGE)"
	# build the source package in the parent directory
	# then rename it to project_version.orig.tar.gz
	$(PYTHON) setup.py $(SETUP_ARGS) sdist $(COMPILE) --dist-dir=$(TARGETDIR) --prune
	#rename -f 's/$(PROJECT)-(.*)\.tar\.gz/$(PROJECT)_$$1\.orig\.tar\.gz/' $(TARGETDIR)/*
	# build the package
	dpkg-buildpackage -i -I -rfakeroot

clean:
	$(PYTHON) setup.py clean
	fakeroot $(MAKE) -f $(CURDIR)/debian/rules clean
	rm -rf build/ MANIFEST
	find . -name '*.pyc' -delete
