(ns spacephoenix.space)

(defn all []
  (.all js/Space))

(defn send-window-to-space-internal [window space-number]
  (let [space (nth (all) (dec space-number))]
    (.moveWindows space (clj->js [window]))))

(defn send-window [window number]
  (send-window-to-space-internal window number))

(defn current []
  (.active js/Space))
