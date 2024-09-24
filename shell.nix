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
          name = "copy" ;
          command = "cp phoenix.js $HOME/.config/phoenix/phoenix.js" ;
        }
      )
      (
        alias {
          ## maybe this should just be symlinked
          name = "copy-dev" ;
          command = "cp phoenix.js $HOME/.phoenix.debug.js" ;
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
          name = "clean-all" ;
          commands = [
            "clean"
            "rm -rf $HOME/phoenix/phoenix.js"
            "rm -rf $HOME/.phoenix.debug.js"
          ] ;
        }
      )
      ( bash-fn {
        name = "run" ;
        commands = ["build" "copy"] ;
      }
      )

      (
        bash-fn {
          name = "run-dev";
          commands = ["build" "copy-dev"] ;
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
