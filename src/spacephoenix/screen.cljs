(ns spacephoenix.screen)

(defn current []
  (.main js/Screen))

(defn windows [screen]
  (.windows screen))

(defn spaces [screen]
  (.spaces screen))

(defn current-space [screen]
  (.currentSpace screen))

(defn height [screen]
  (.-height (.flippedVisibleFrame screen)))

(defn width [screen]
  (.-width (.flippedVisibleFrame screen)))

(defn x [screen]
  (.-x (.flippedVisibleFrame screen)))

(defn y [screen]
  (.-y (.flippedVisibleFrame screen)))

(defn current-size []
  (let [current-screen (current)]
    {:height (height current-screen)
     :width (width current-screen)}))

(defn current-size-and-position []
  (let [current-screen (current)]
    {:height (height current-screen)
     :width (width current-screen)
     :x      (x current-screen)
     :y      (y current-screen)}))
