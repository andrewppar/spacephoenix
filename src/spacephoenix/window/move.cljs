(ns spacephoenix.window.move
  (:require [spacephoenix.space :as space]
            [spacephoenix.window.core :as window]
            [spacephoenix.window.yabai :as yabai]))

;; come up with a better way of doing this configuration...

(defn ^:private yabai-move [direction]
  (when yabai/executable
    (yabai/window "--warp" (name direction))))

(defn east []
  (yabai-move :east))

(defn north []
  (yabai-move :north))

(defn west []
  (yabai-move :west))

(defn south []
  (yabai-move :south))

#_
(defn to-space [number & {:keys [system] :or {system :phoenix}}]
  (case system
    :yabai (yabai/window "--space" (str number))
    :phoenix (space/send-window (window/focused) (str number))))
