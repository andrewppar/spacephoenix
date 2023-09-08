(ns spacephoenix.space
  (:require
   [clojure.string :as string]
   [spacephoenix.app :as app]
   [spacephoenix.screen :as screen]
   [spacephoenix.process :as proc]
   [spacephoenix.window :as window]))

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
    (proc/applescript script)))

(defn current []
  (.active js/Space))

(defn windows [space]
  (.windows space))

(defn layout []
    (println
    (mapv (fn [s]
            (mapv window/title (windows s)))
          (all))))
