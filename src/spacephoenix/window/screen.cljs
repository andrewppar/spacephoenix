(ns spacephoenix.window.screen
  (:require
   [spacephoenix.keys :as keys]
   [spacephoenix.message :as message]
   [spacephoenix.screen :as screen]
   [spacephoenix.window.core :as window]
   [spacephoenix.timer :as timer]))

(defn unbind-all-screen-keys []
  (keys/unbind-keys-of-type ::screen))

(defn bind-move [window timer alerts {:keys [idx screen]}]
  (keys/bind
   (str idx)
   (fn []
     (timer/off timer)
     (unbind-all-screen-keys)
     (run! message/close-alert alerts)
     (screen/move-window window screen))
   :key-type ::screen))

(defn make-screen-box [{:keys [idx screen]}]
  (let [{:keys [x y height width]} (screen/size-and-position screen)
        mid-x (+ x (/ width 2))
        mid-y (+ y (/ height 2))]
    (message/alert (str "screen " idx)
                   :duration 5
                   :x-coord (- mid-x 10)
                   :y-coord (- mid-y 10))))

(defn numbered-screens []
  (let [screens (screen/all)]
    (loop [idx 1
           screen (first screens)
           todo (rest screens)
           result []]
      (let [next-result (conj result {:idx idx :screen screen})]
        (if (seq todo)
          (recur (inc idx) (first todo) (rest todo) next-result)
          next-result)))))

(defn to-screen! []
  (let [timer (timer/make 5 (fn [] (unbind-all-screen-keys)))
        window (window/focused)
        screen-maps (numbered-screens)
        alerts (mapv make-screen-box screen-maps)]
    (run! (partial bind-move window timer alerts) screen-maps)))
