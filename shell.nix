let
  pkgs = import <nixpkgs> {} ;

  # aliases
  alias = {name, command}: "alias " + name + "=\"" + command + "\"" ;

  aliases = builtins.foldl'
    (acc: als: acc + als + "\n")
    ""
    [ (
      alias {
        name = "build" ;
        command = "clj -M -m cljs.main -co build.edn -c" ;
      }
    )
      (
        alias {
          name = "clean" ;
          command = "rm -rf out" ;
        }
      )
      (
        alias {
          ## maybe this should just be symlinked
          name = "copy-dev" ;
          command = "cp phoenix.js ~/.phoenix.debug.js" ;
        }
      )
    ] ;

  bash-fn = {name, commands}:
    let command-string = builtins.foldl'
      (acc: cmd: acc + cmd + "\n")
      ""
      commands ;
    in
      "function " + name + "() {\n"
      + command-string
      + "\n};\n" ;

  functions = builtins.foldl'
    (acc: fn: acc + fn + "\n")
    ""
    [
      (
        bash-fn {
          name = "build-dev";
          commands = ["build" "copy-dev"] ;
        }
      )
      (
        bash-fn {
          name = "clean-all" ;
          commands = [ "clean" "rm -rf ../phoenix/phoenix.js" ] ;
        }
      )
    ] ;
in

pkgs.mkShell {
  packages = with pkgs; [
    clojure
  ];

  shellHook =  aliases
               + functions
               + ''echo "weclome to spacephoenix..."'';

}
