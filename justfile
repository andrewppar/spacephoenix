clean:
	rm -rf out

clean-all: clean
	rm -rf ../phoenix/phoenix.js

build:
	clj -M -m cljs.main -co build.edn -c

copy-dev:
	cp ../phoenix/phoenix.js ~/.phoenix.debug.js

build-dev: build copy-dev
