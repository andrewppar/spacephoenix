stop:
	osascript -e "tell application \"Phoenix\" to quit"

start:
	open -a /Applications/Phoenix.app

install:
	rm -rf phoenix.js
	nix build
	ln -s ./result/phoenix.js phoenix.js

clean:
	rm -rf out

clean-all: clean
	rm -rf phoenix.js

run: stop install start
