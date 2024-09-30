(ns spacephoenix.utils
  (:require [clojure.java.io :as io]))

(defmacro defn-timed [name args & body]
  `(defn ~name ~args
     (let [start# (.now js/Date)
           result# (do ~@body)]
       #_(println "HELLO!!!!")
       (println (str "WHAT time: " (- (.now js/Date) start#)))
       result#)))
