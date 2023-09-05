(ns phoenix.window)

(defn focused []
  (.focused js/Window))

(defn maximize [window]
  (.maximize window))

(defn maximize-focused []
  (maximize (focused)))

(defn minimize [window]
  (.minimize window))

(defn minimize-focused []
  (minimize (focused)))

(defn focus [window]
  (.focus window))

(defn title [window]
  (.title window))

(defn normal? [window]
  (= 1 (.isNormal window)))

(defn spaces [window]
  (.spaces window))

(defn all [& {:keys [only-normal?]
              :or {only-normal? true}}]
  (cond->> (.all js/Window)
    only-normal? (filter normal?)))

(defn move-to [window {:keys [x y h w]}]
  (.setTopLeft window (clj->js {:x x :y y}))
  (.setSize window (clj->js {:width w :height h}))
  window)

(defn close [window]
  (.close window))

(defn close-focused []
  (close (focused)))
