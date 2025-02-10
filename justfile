clean:
	rm -rf phoenix.js

clean-all: clean
	rm -rf out

install: clean
	nix build
	ln -s ./result/phoenix.js phoenix.js

run: stop install start

start:
	open -a /Applications/Phoenix.app

stop:
	osascript -e "tell application \"Phoenix\" to quit"
