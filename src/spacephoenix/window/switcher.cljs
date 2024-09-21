(ns spacephoenix.window.switcher
  (:require
   [spacephoenix.keys :as keys]
   [spacephoenix.message :as message]
   [spacephoenix.screen :as screen]
   [spacephoenix.timer :as timer]
   [spacephoenix.window.core :as window]))

(defn show-numbered-windows []
  (message/alert
   (get
    (reduce
     (fn [{:keys [count result]} window]
       {:count (inc count)
        :result (str result "\n" count ": " (window/title window))})
     {:count 0 :result ""}
     (window/all))
    :result)))

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
          half-screen (/ height 2)
          y (.-y top-left)
          reflect-distance (- y half-screen)
          new-pos (- (+ half-screen (- reflect-distance)) 60)]
      (message/alert (str number)
                     :duration 5
                     :x-coord x
                     :y-coord new-pos))))

(defn switch! []
  (let [window-map (reduce
                    (fn [{:keys [count] :as acc} window]
                      (let [title (window/title window)]
                        (if (contains? titles-to-filter title)
                          acc
                          (let [entry {:title title
                                       :window window}]
                            (assoc acc
                                   :count (inc count)
                                   count entry)))))
                    {:count 0}
                    (window/all))
        timer (timer/make 5 (fn [] (unbind-all-window-keys)))
        alerts (reduce-kv
                (fn [acc idx {:keys [window]}]
                  (if-not (= idx :count)
                    (conj acc (make-window-box idx window))
                    acc))
                []
                window-map)]
    (run!
     (fn [[idx {:keys [window]}]]
       (when-not (= idx :count)
         (bind-switch idx window timer alerts)))
     window-map)))
