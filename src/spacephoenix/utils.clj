(ns spacephoenix.utils
  (:require [clojure.java.io :as io]))

(defmacro defn-timed [name args log-spec & body]
  `(defn ~name ~args
     (let [start# (.now js/Date)
           result# (do ~@body)]
       #_(println "HELLO!!!!")
       (println (str ~(get log-spec :name) " time: " (- (.now js/Date) start#)))
       result#)))
