(ns phoenix.space
  (:require
   [clojure.string :as string]
   [phoenix.app :as app]
   [phoenix.screen :as screen]
   [phoenix.shell :as shell]
   [phoenix.window :as window]))

(defn all []
  (.all js/Space))

(defn send-focused-window-to-space [space-number]
  (let [window (window/focused)
        space  (nth (all) (dec space-number))]
    (.moveWindows space (clj->js [window]))
    (window/focus window)))

(defn focus [space-number config]
  (let [keynum  (+ 17 space-number)
        modifier (str "{"
                      (->> (get config :switch-space-modifiers)
                           (mapv (fn [key]
                                   (str (name key) " down")))
                           (string/join
                            ","))
                      "}")
        script  (str "tell application \"System Events\" to key code "
                     keynum
                     " using "
                     modifier)]
    (println script)
    (shell/applescript script)))

(defn current []
  (.active js/Space))

(defn windows [space]
  (.windows space))
