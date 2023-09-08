(ns spacephoenix.message
  (:require
   [clojure.string :as string]
   [spacephoenix.screen :as screen]))

(defn notify
  [^String message]
  (.notify js/Phoenix message))

(defn alert
  [message & {:keys [duration] :or {duration 2}}]
  (let [modal (js/Modal.)
        {:keys [height width]} (screen/current-size)]
    (set! (.-origin modal)
          #js
          {:x (/ width  2)
           :y (/ height 2)})
    (set! (.-text modal) (string/join " " message))
    (set! (.-duration modal) duration)
    (.show modal)
    modal))

(defn close-alert [alert]
  (.close alert))
