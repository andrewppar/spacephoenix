(ns spacephoenix.space
  (:require
   [clojure.string :as string]
   [spacephoenix.app :as app]
   [spacephoenix.screen :as screen]
   [spacephoenix.process :as proc]
   [spacephoenix.window.core :as window]))

(defn all []
  (.all js/Space))

(defn send-focused-window-to-space-internal [window space-number]
  (let [space  (nth (all) (dec space-number))]
    (.moveWindows space (clj->js [window]))))

(defn send-focused-window-to-space [space-number]
  (let [window (window/focused)]
    (send-focused-window-to-space-internal window space-number)))

(defn send-focused-window-to-space-and-refocus [space-number]
  (let [window (window/focused)]
    (send-focused-window-to-space-internal window space-number)
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
