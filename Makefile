##
## Makefile for Opal developers
##
version=5.1-SNAPSHOT
magma_version=5.0-SNAPSHOT
commons_version=4.1-SNAPSHOT
java_opts="-Xms1G -Xmx4G -XX:+UseG1GC"
#java_opts="-Xms1G -Xmx4G"

projects=$(CURDIR)/..
opal_project=$(CURDIR)
magma_project=${projects}/magma
commons_project=${projects}/obiba-commons
ds4j_project=${projects}/datashield4j
jennite_project=${projects}/jennite
obiba_home=${projects}/obiba-home

ifdef OPAL_HOME
	opal_home = ${OPAL_HOME}
else
	opal_home = ${opal_project}/opal_home
endif

skipTests=false
mvn_exec=mvn -Dmaven.test.skip=${skipTests}
orientdb_version=3.2.27
hsqldb_version=2.7.1

mysql_root=root
mysql_password=1234
opal_db=opal_dev
key_db=key_dev

#
# Compile Opal and prepare Opal server
#
all: clean compile server

#
# Clean Opal
#
clean:
	cd ${opal_project} && \
	${mvn_exec} clean

#
# Compile Opal
#
compile:
	cd ${opal_project} && \
	${mvn_exec} -U install

#
# Compile Opal without compiling GWT
#
compile-no-gwt:
	cd ${opal_project} && \
	${mvn_exec} install -Dgwt.compiler.skip=true

#
# Update Opal source code
#
update:
	cd ${opal_project} && \
	git pull

#
# Unzip Opal distribution
#
server:
	cd ${opal_project}/opal-server/target && \
	unzip opal-server-${version}-dist.zip

#
# Launch Opal
#
run:
	export OPAL_HOME=${opal_home} && \
	export JAVA_OPTS=${java_opts} && \
	sed -i 's/^java $$JAVA_OPTS $$JAVA_DEBUG/java $$JAVA_OPTS/g' ${opal_project}/opal-server/target/opal-server-${version}/bin/opal && \
	${opal_project}/opal-server/target/opal-server-${version}/bin/opal

#
# Launch Opal in debug mode
#
debug:
	export OPAL_HOME=${opal_home} && \
	export JAVA_OPTS=${java_opts} && \
	sed -i 's/^java $$JAVA_OPTS $$JAVA_DEBUG/java $$JAVA_OPTS/g' ${opal_project}/opal-server/target/opal-server-${version}/bin/opal && \
	sed -i 's/^java $$JAVA_OPTS/java $$JAVA_OPTS $$JAVA_DEBUG/g' ${opal_project}/opal-server/target/opal-server-${version}/bin/opal && \
	${opal_project}/opal-server/target/opal-server-${version}/bin/opal

#
# Prepare opal home
#
prepare: conf fs

conf:
	mkdir -p ${opal_home} && \
	cp -r ${opal_project}/opal-server/src/main/conf ${opal_home}

fs:
	mkdir -p ${opal_home} && \
	cp -r ${obiba_home}/opal/seed/fs ${opal_home}

#
# Compile and install a Opal sub-project
#
opal:
	cd ${opal_project}/${p} && \
	${mvn_exec} clean install && \
	cp target/${p}-${version}.jar ${opal_project}/opal-server/target/opal-server-${version}/lib

#
# Update Magma source code
#
magma-update:
	cd ${magma_project} && \
	git pull

#
# Compile and install a Magma sub-project
#
magma:
	cd ${magma_project}/${p} && \
	${mvn_exec} clean install && \
	cp target/${p}-${magma_version}.jar ${opal_project}/opal-server/target/opal-server-${version}/lib

#
# Compile and install all Magma sub-projects
#
magma-all:
	cd ${magma_project} && \
	${mvn_exec} clean install && \
	find ${opal_project}/opal-server/target/opal-server-${version}/lib -type f | grep magma | xargs rm && \
	cp `find . -type f | grep jar$$ | grep -v sources | grep -v javadoc | grep -v jacoco | grep -v magma-filter | grep -v magma-beans | grep -v magma-data-generator | grep -v magma-spring | grep -v magma-test | grep -v magma-integration` ${opal_project}/opal-server/target/opal-server-${version}/lib

#
# Compile and install a Commons sub-project
#
commons:
	cd ${commons_project}/${p} && \
	${mvn_exec} clean install && \
	cp target/${p}-${commons_version}.jar ${opal_project}/opal-server/target/opal-server-${version}/lib

#
# Compile and install all Commons sub-projects
#
commons-all:
	cd ${commons_project} && \
	${mvn_exec} clean install && \
	find ${opal_project}/opal-server/target/opal-server-${version}/lib -type f | grep obiba- | xargs rm && \
	cp `find . -type f | grep jar$$ | grep -v sources | grep -v javadoc` ${opal_project}/opal-server/target/opal-server-${version}/lib && \
	rm ${opal_project}/opal-server/target/opal-server-${version}/lib/obiba-*-cli.jar && \
	rm ${opal_project}/opal-server/target/opal-server-${version}/lib/obiba-jersey-*.jar && \
	rm ${opal_project}/opal-server/target/opal-server-${version}/lib/obiba-mongodb-*.jar

#
# Compile and install all DataSHIELD4J sub-projects
#
ds4j-all:
	cd ${ds4j_project} && \
	${mvn_exec} clean install && \
	find ${opal_project}/opal-server/target/opal-server-${version}/lib -type f | grep ds4j | xargs rm && \
	cp `find . -type f | grep jar$$ | grep -v sources | grep -v javadoc` ${opal_project}/opal-server/target/opal-server-${version}/lib


#
# Compile and install jennite
#
jennite:
	cd ${jennite_project} && \
	${mvn_exec} clean install && \
	cp ${jennite_project}/jennite-vcf-store/target/*-dist.zip ${opal_home}/plugins/

#
# Tail Opal log file
#
log:
	tail -f ${opal_home}/logs/opal.log

log-agent:
	logagent --yaml -g '${opal_home}/logs/*.log'

#
# Delete all log files
#
clear-log:
	rm ${opal_home}/logs/*

#
# Delete ES indexes
#
clear-data:
	rm -rf ${opal_home}/data/opal/*

clear-config:
	rm -rf ${opal_home}/data && \
	rm -rf ${opal_home}/conf && \
	rm -rf ${opal_home}/logs && \
	rm -rf ${opal_home}/work

check-updates:
	cd ${opal_project} && ${mvn_exec} versions:display-dependency-updates

check-plugin-updates:
	cd ${opal_project} && ${mvn_exec} versions:display-plugin-updates

check-magma-updates:
	cd ${magma_project} && ${mvn_exec} versions:display-dependency-updates

#
# Dump MySQL databases
#
sql-dump: sql-opal-dump sql-key-dump

sql-opal-dump:
	mysqldump -u $(mysql_root) --password=$(mysql_password) --hex-blob --max_allowed_packet=1G $(opal_db) > $(opal_db)_$(version)_dump.sql

sql-key-dump:
	mysqldump -u $(mysql_root) --password=$(mysql_password) $(key_db) > $(key_db)_$(version)_dump.sql

#
# Drop databases and import SQL dump
#
sql-import: sql-opal-import sql-key-import

sql-opal-import:
	mysql -u $(mysql_root) --password=$(mysql_password) -e "drop database `$(opal_db)`; create database `$(opal_db)`;" && \
	mysql -u $(mysql_root) --password=$(mysql_password) `$(opal_db)` < $(opal_db)_$(version)_dump.sql

sql-key-import:
	mysql -u $(mysql_root) --password=$(mysql_password) -e "drop database `$(key_db)`; create database `$(key_db)`;" && \
	mysql -u $(mysql_root) --password=$(mysql_password) `$(key_db)` < $(key_db)_$(version)_dump.sql

download-orientdb:
	mkdir -p ${opal_home}/work && \
	cd ${opal_home}/work && \
	wget "https://repo1.maven.org/maven2/com/orientechnologies/orientdb-community/$(orientdb_version)/orientdb-community-$(orientdb_version).zip" && \
	unzip orientdb-community-$(orientdb_version).zip && \
	rm orientdb-community-$(orientdb_version).zip && \
	chmod a+x orientdb-community-$(orientdb_version)/bin/*.sh

orientdb-console:
	@echo
	@echo "To connect to Opal OrientDB:"
	@echo "  connect premote:localhost:2424/opal-config admin admin"
	@echo "or"
	@echo "  connect plocal:$(opal_home)/data/orientdb/opal-config admin admin"
	@echo
	@cd ${opal_home}/work/orientdb-community-$(orientdb_version)/bin && ./console.sh

hsqldb-tool:
	cd ${opal_home} && \
	java -jar ../opal-server/target/opal-server-$(version)/lib/hsqldb-$(hsqldb_version).jar --url jdbc:hsqldb:file:data/hsql/opal_config --user opal --password $(hsqldb_pwd)

#
# GWT locales
#
locales:
	cd ${opal_project}/opal-gwt-client && \
	${mvn_exec} antrun:run

#
# Plugins
#

plugins: search-plugin datasource-plugins analysis-plugins

vcf-plugin: jennite

search-plugin:
	$(call install-plugin,opal-search-es)

datasource-plugins: datasource-file-plugins datasource-server-plugins

datasource-file-plugins:
	$(call install-plugin,opal-datasource-spss)
	$(call install-plugin,opal-datasource-readr)
	$(call install-plugin,opal-datasource-readxl)

datasource-server-plugins:
	$(call install-plugin,opal-datasource-limesurvey)
	$(call install-plugin,opal-datasource-redcap)
	$(call install-plugin,opal-datasource-googlesheets4)

analysis-plugins:
	$(call install-plugin,opal-analysis-validate)

plugin:
	$(call install-plugin,${p})

#
# Functions
#
install-plugin = cd ${projects}/$(1) && mvn clean install && cp target/$(1)-*-dist.zip ${opal_home}/plugins/
