(ns spacephoenix.yabai.core)

(defn move-window-right []
  (.run
   js/Task
   "/run/current-system/sw/bin/yabai"
   (clj->js ["-m" "window" "--warp" "east"])))
