(ns spacephoenix.window.switcher
  (:require
   [spacephoenix.keys :as keys]
   [spacephoenix.message :as message]
   [spacephoenix.screen :as screen]
   [spacephoenix.timer :as timer]
   [spacephoenix.window.core :as window]))

(def titles-to-filter
  #{"Window"
    "Notification Center"})

(defn unbind-all-window-keys []
  (keys/unbind-keys-of-type ::window))

(defn bind-switch [key window timer alerts]
  (keys/bind
   (str key)
   (fn []
     (timer/off timer)
     (unbind-all-window-keys)
     (run! message/close-alert alerts)
     (window/focus window))
   :key-type ::window))

(defn make-window-box [number window]
  (when window
    (let [top-left (window/top-left window)
          x (.-x top-left)
          {:keys [height]} (screen/current-size-and-position)
          title (window/title window)
          half-screen (/ height 2)
          y (.-y top-left)
          reflect-distance (- y half-screen)
          new-pos (- (+ half-screen (- reflect-distance)) 60)]
      (message/alert (str number ": " title)
                     :duration 5
                     :x-coord x
                     :y-coord new-pos))))

(defn numbered-windows []
  (let [windows (window/all)]
    (loop [count 0
           window (first windows)
           todo (rest windows)
           result []]
      (if window
        (let [title (window/title window)
              remove? (or (contains? titles-to-filter title)
                        (not (window/normal? window))
                        (window/minimized? window))
              new-result (if remove?
                           result
                           (conj result {:title title :window window :idx count}))
              new-count (if remove? count (inc count))]
          (recur new-count (first todo) (rest todo) new-result))
        result))))

(defn switch! []
  (let [timer (timer/make 5 (fn [] (unbind-all-window-keys)))
        window-map (numbered-windows)
        alerts (mapv
                (fn [{:keys [idx window]}] (make-window-box idx window))
                window-map)]
    (run!
     (fn [{:keys [idx window]}] (bind-switch idx window timer alerts))
     window-map)))
