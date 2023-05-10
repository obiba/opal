# Opal [![Build Status](https://app.travis-ci.com/obiba/opal.svg?branch=master)](https://app.travis-ci.com/github/obiba/opal)

Opal is OBiBa’s core database application for biobanks or epidemiological studies.

* See [download instructions](http://www.obiba.org/pages/products/opal/#download).
* Read the [documentation](http://opaldoc.obiba.org).
* Have a bug or a question? Please create an issue on [GitHub](https://github.com/obiba/opal/issues).
* Continuous integration is on [Travis](https://travis-ci.org/obiba/opal).

## Developers

Set up a development environment:

* Make sure Java8 is available.
* Make sure you have a MongoDB, MySQL or MariaDB database running.
* Compile with `make all`
* Get the [obiba-home](https://github.com/obiba/obiba-home) repository to have some seeding material.
* Prepare execution environment: `make prepare` (do it only once, this will create a opal_home directory)
* Run in debug mode: `make debug`
* Login http://localhost:8080 with administrator/password credentials.
* Declare the databases (only at the first connection)
* Go to _obiba-home_ project and `make seed-opal`

There are many other targets, just check the _Makefile_ and change variables to match your environment.

## Mailing list

Have a question? Ask on our mailing list!

obiba-users@googlegroups.com

[http://groups.google.com/group/obiba-users](http://groups.google.com/group/obiba-users)

## License

OBiBa software are open source and made available under the [GPL3 licence](http://www.obiba.org/pages/license/). OBiBa software are free of charge.

# OBiBa acknowledgments

If you are using OBiBa software, please cite our work in your code, websites, publications or reports.

"The work presented herein was made possible using the OBiBa suite (www.obiba.org), a  software suite developed by Maelstrom Research (www.maelstrom-research.org) and Epigeny (www.epigeny.io)"
