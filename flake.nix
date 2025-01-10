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
            ''${pkgs.openjdk}/bin/java -cp ${cljs}:src cljs.main -co build.edn -O advanced -c''
          ] ;
        installSteps =
          [
            ''mkdir -p $out''
            ''cp phoenix.js $out/phoenix.js''
          ] ;
        buildDependencies = with pkgs; [ clojure openjdk] ;
        # this doesn't get used in builds, but it is nice to have
        # it in nix develop - probably should use nix shell nixpkgs#just
        # if there's good tooling for that
        devDependencies = with pkgs; [ just ];
      in {
        packages.default = pkgs.stdenv.mkDerivation {
          name = "spacephoenix" ;
          version = "0.0.1" ;
          src = ./. ;
          buildInputs = buildDependencies ++ devDependencies ;
          buildPhase = builtins.concatStringsSep "\n" buildSteps ;
          installPhase = builtins.concatStringsSep "\n" installSteps ;
        } ;
      }) ;
}
