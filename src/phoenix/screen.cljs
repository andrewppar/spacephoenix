(ns phoenix.screen)

(defn current []
  (.main js/Screen))

(defn height [screen]
  (.-height (.flippedVisibleFrame screen)))

(defn width [screen]
  (.-width (.flippedVisibleFrame screen)))
