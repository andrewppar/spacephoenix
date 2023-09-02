clean:
	rm -rf out

clean-all: clean
	rm -rf phoenix.js

build:
	clj -M -m cljs.main -co build.edn -c
