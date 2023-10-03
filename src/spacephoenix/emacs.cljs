(ns spacephoenix.emacs
  (:require [spacephoenix.screen :as screen]
            [spacephoenix.process :as proc]))

(defn capture []
  (let [{:keys [height width]} (screen/current-size-and-position)
        modal (js/Modal.)]
    (set! (.-isInput modal) (clj->js true))
    (set! (.-appearance modal) (clj->js "dark"))
    (set! (.-origin modal) (clj->js {"x" (/ width 2)
                                     "y" (/ height 2)}))
    (set! (.-textDidCommit modal) (clj->js
                                   (fn [value _]
                                     (proc/capture value)
                                     (.close modal))))


    (.show modal)))
