PROTO_DIR := ../opal-web-model/src/main/protobuf

SOURCES := $(wildcard ${PROTO_DIR}/*.proto)

DESTINATION := ./src/models

.PHONY: proto

proto:
	mkdir -p ${DESTINATION}
	@for file in $(SOURCES); do \
    base_name=$$(basename $$file); \
		echo "Processing $$base_name"; \
    rm -f ${DESTINATION}/$${base_name%.*}.ts; \
    protoc --plugin=./node_modules/.bin/protoc-gen-ts_proto --ts_proto_opt=onlyTypes=true --ts_proto_opt=outputJsonMethods=false --ts_proto_opt=outputEncodeMethods=false --ts_proto_opt=stringEnums=true --ts_proto_opt=outputExtensions=true --ts_proto_out=${DESTINATION} --proto_path=${PROTO_DIR} $$base_name; \
	done
