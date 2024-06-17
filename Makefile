build:
	clj -M -m cljs.main -co build.edn -c

build-dev: build copy-dev

clean-all: clean
	rm -rf ../phoenix/phoenix.js

copy-dev:
	cp ../spacephoenix/phoenix.js ~/.phoenix.debug.js

clean:
	rm -rf out
