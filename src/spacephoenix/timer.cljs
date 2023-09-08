(ns spacephoenix.timer)

(defn make [interval callback
            & {:keys [repeat?] :or {repeat? false}}]
  (if repeat?
    (.every js/Timer interval callback)
    (.after js/Timer interval callback)))

(defn off [timer]
  (.off js/Timer timer))
