{
  description = "Manage Mac from your keyboard" ;
  inputs = {
    nipkgs.url = "github:nixos/nixpkgs/nixos-unstable" ;
    flake-utils.url = "github:numtide/flake-utils" ;
  };
  outputs = {self, nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system ;};
        cljs = pkgs.fetchurl {
          hash = "sha256-2xPFeMvpvErBL2KFbbcx2iMXENovsrk+3bubntp78tc=" ;
          url = "https://github.com/clojure/clojurescript/releases/download/r1.11.60/cljs.jar";
        } ;
        buildSteps =
          [
            ''export HOME=$PWD/src''
            ''export HOST="${(builtins.getEnv "SP_HOST")}"''
            ''echo $HOST''
            ''rm -rf phoenix.js''
            ''${pkgs.openjdk}/bin/java -cp ${cljs}:src cljs.main -co build.edn -O advanced -c''
          ] ;
        installSteps =
          [
            ''mkdir -p $out''
            ''cp phoenix.js $out/phoenix.js''
          ] ;
        buildDependencies = with pkgs; [ clojure openjdk] ;
      in {
        packages.default = pkgs.stdenv.mkDerivation {
          name = "spacephoenix" ;
          version = "0.0.1" ;
          src = ./. ;
          buildInputs = buildDependencies ;
          buildPhase = builtins.concatStringsSep "\n" buildSteps ;
          installPhase = builtins.concatStringsSep "\n" installSteps ;
        } ;
        devShells.default =
          let
            shell-fn = {name, commands}:
              ("function " + name + "() {\n")
              + builtins.concatStringsSep "\n" commands
              + "\n}\n" ;
            fns = builtins.concatStringsSep "\n"
              [(shell-fn {name = "clean"; commands = ["rm -rf phoenix.js"];})
               (shell-fn {name = "clean-all"; commands = ["clean" "rm -rf out"];})
               (shell-fn {name = "install";
                          commands = [
                            "clean"
                            "nix build"
                            "ln -s ./result/phoenix.js phoenix.js"];})
               (shell-fn {name = "run"; commands = ["stop" "install" "start"];})
               (shell-fn {name = "start"; commands = ["open -a /Applications/Phoenix.app"];})
               (shell-fn {
                 name = "stop";
                 commands = [
                   ''osascript -e "tell application \"Phoenix\" to quit"''
                 ];})] ;
          in pkgs.mkShell {
            packages = buildDependencies ;
            shellHook = fns + ''echo "mac <3 clojurescript"'' ;
          } ;
      }) ;
}
