(ns spacephoenix.window.yabai)

(def executable "/run/current-system/sw/bin/yabai")

(defn yabai [& {:keys [args]}]
  (.run js/Task executable (clj->js (into ["-m"] args)) identity))

(defn window [& args]
  (yabai :args (into ["window"] args)))

(defn sleep [timeout]
  (let [maxtime (+ (.getTime (js/Date.)) timeout)]
    (while (< (.getTime (js/Date.)) maxtime))))

(defn window-list []
  (let [task (.run js/Task (clj->js ["-m" "query" "--window"])
                   (fn [task] (.-output task)))]
    (dotimes [n 10]
      (sleep 1000)
      (println (str "checking task " n "th time"))
      (println task))))
