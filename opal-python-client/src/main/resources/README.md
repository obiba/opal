# Opal Python

This Python-based command line tool allows to access to a Opal server through its REST API. This is the perfect tool
for automating tasks in Opal. This will be the preferred client developed when new features are added to the REST API.

### Dependencies

You need to have pycurl installed within your Python install:

http://pycurl.sourceforge.net/

```
sudo apt-get install python-pycurl
```

You will also need the Google's Protobuf bindings for Python to be installed:

https://code.google.com/p/protobuf/

```
sudo apt-get install python-protobuf
```

### Usage

To get the options of the command line:

```
opal.py --help
```

This command will display which sub-commands are available. For each sub-command you can get the help message as well:

```
opal.py <subcommand> --help
```

The objective of having sub-command is to hide the complexity of applying some use cases to the Opal REST API. More
sub-commands will be developed in the future.

### Development

Opal Python client can be easily extended by using the classes defined in `core.py` and in `protobuf/*.py` files.

### User Guide

A user guide with a detailed list of commands and options can be found [here](http://wiki.obiba.org/display/OPALDOCDEV/Opal+Python+User+Guide).
