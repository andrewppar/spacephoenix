(ns spacephoenix.utils)

(defmacro defn-timed [name args & body]
  `(defn ~name ~args
     (let [start# (.now js/Date)
           result# (do ~@body)]
       (println "HELLO!!!!")
       (println (str "WHAT time: " (- (.now js/Date) start#)))
       result#)))
