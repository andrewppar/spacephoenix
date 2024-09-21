(ns spacephoenix.custom
  (:require [spacephoenix.message :as message]
            [spacephoenix.process :as proc]))

(defn video-call []
  {:b {:title "Brooke"
       :action
       (fn []
         (proc/browse-url
          "https://us06web.zoom.us/j/5142946429?pwd=ZDVTTndHYm9EKzRiSVBycWJ0NC8wdz09"))}
   :e  {:title "Eric"
        :action
        (fn []
          (message/alert
           "https://us06web.zoom.us/j/4684776846?pwd=Szd3VUhINDloM1BHdEdwMmNNWkNndz09"))}
   :i  {:title "Me"
        :action
        (fn []
          (proc/browse-url
           "https://cisco.webex.com/meet/anparisi"))}
   :m  {:title "Mike"
        :action
        (fn []
          (proc/browse-url
           "https://us02web.zoom.us/j/6214579943?pwd=aXlJUkM3d1d3SENQbk42aXZUTW9OQT0"))}})

(def menu
  {:m {:title "meeting"
       :items (video-call)}})
