(ns spacephoenix.custom
  (:require [spacephoenix.message :as message]
            [spacephoenix.process :as proc]))

(defn video-call [])

(def menu
  {:m {:title "meeting"
       :items (video-call)}})
