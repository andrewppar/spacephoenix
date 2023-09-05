(ns phoenix.screen)

(defn current []
  (.main js/Screen))

(defn height [screen]
  (.-height (.flippedVisibleFrame screen)))

(defn width [screen]
  (.-width (.flippedVisibleFrame screen)))

(defn current-size []
  (let [current-screen (current)]
    {:height (height current-screen)
     :width (width current-screen)}))
