(ns phoenix.message
  (:require [clojure.string :as string]))

(defn notify
  [^String message]
  (.notify js/Phoenix message))

(defn alert
  [message & {:keys [duration] :or {duration 2}}]
  (let [modal (js/Modal.)
        main-screen-rect (.flippedVisibleFrame (.main js/Screen))]
    (set! (.-origin modal)
          #js
          {:x (/ (.-width main-screen-rect) 2)
           :y (/ (.-height main-screen-rect) 2)})
    (set! (.-text modal) (string/join " " message))
    (set! (.-duration modal) duration)
    (.show modal)
    modal))

(defn close-alert [alert]
  (.close alert))
