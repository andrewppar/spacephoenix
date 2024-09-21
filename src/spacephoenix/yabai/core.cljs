(ns spacephoenix.yabai.core)

(defn []
  (.run
   js/Task
   "/run/current-system/sw/bin/yabai"
   (clj->js ["-m" "window" "--warp" "east"])))
