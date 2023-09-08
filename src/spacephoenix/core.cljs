(ns spacephoenix.core
  (:require
   [clojure.string :as string]
   [spacephoenix.app :as app]
   [spacephoenix.keys :as keys]
   [spacephoenix.menu :as menu]
   [spacephoenix.message :as message]
   [spacephoenix.process :as proc]
   [spacephoenix.space :as space]
   [spacephoenix.tile :as tile]
   [spacephoenix.window :as window]))

(message/alert "Welcome to SpacePhoenix")

(defn exit []
  {:title "Exit"
   :action
   (fn [] (menu/unbind-all-menu-keys))})

(defn make-menu [base-config]
  (assoc base-config :escape (exit)))

(def config
  {:switch-space-modifiers [:command]})

;; Use grammarly config for this?
(defn apps []
  (make-menu
   {:a {:title "Alfred"
        :action (fn [] (app/launch "Alfred 5"))}
    :b {:title "Brave Browser"
        :action (fn [] (app/launch "Brave Browser"))}
    :f {:title "Finder"
        :action (fn [] (app/launch "Finder"))}
    :i {:title "iTerm"
        :action (fn [] (app/launch "iterm"))}
    :m {:title "Mail"
        :action (fn [] (app/launch "Mail"))}
    :q {:title "Quit"
        :action app/quit-focused}
    :w {:title "Webex"
        :action (fn [] (app/launch "Webex"))}
    :s {:title "Safari"
        :action (fn [] (app/launch "Safari"))}}))

(defn tile []
  (make-menu
   {:t {:title "Tile"
        :action (fn [] (tile/tile))}
    :s {:title "Start Auto-Tile"
        :action (fn [] (tile/start-auto-tile))}
    :q {:title "Stop Auto-Tile"
        :action (fn [] (tile/stop-auto-tile))}}))

(defn windows []
  (let [initial-map {:m {:title "Maximize"
                         :action window/maximize-focused}
                     :z {:title "Minimize"
                         :action window/minimize-focused}
                     :q {:title "Close"
                         :action window/close-focused}}
        space-count (count (space/all))
        space-numbers  (range 1 (inc space-count))]
    (make-menu
     (reduce
      (fn [result number]
        (let [number-string (str number)]
          (assoc result (keyword number-string)
                 {:title (str "Follow App to Space " number-string)
                  :action
                  (fn [] (space/send-focused-window-to-space number))})))
      initial-map
      space-numbers))))

(defn switch-space-bindings []
  (let [space-nums (range 1 (inc (count (space/all))))]
    (reduce
     (fn [acc space-number]
       (assoc acc
              (keyword (str space-number))
              {:action (fn [] (space/focus space-number config))}))
     {:nil {:title (str "Switch to space: "
                        (string/join ", " space-nums))}}
     space-nums)))


(defn video-call []
  {:b {:title "Brooke"
       :action
       (fn []
         (proc/browse-url
          "https://us06web.zoom.us/j/5142946429?pwd=ZDVTTndHYm9EKzRiSVBycWJ0NC8wdz09"))}
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

(defn menu []
  {:title "Menu"
   :items
   (make-menu
    (merge
     (switch-space-bindings)
     {:space {:title "Alfred"
              :action
              (fn [] (app/launch "Alfred 5"))}
      :a      {:title "Apps"
               :items (apps)}
      :t      {:title "Tile"
               :items (tile)}
;;      :x      {:title "TEST"
;;               :action (space/layout)}
      :w      {:title "Window"
               :items (windows)}
      :g      {:title "Quit"
               :modifiers [:ctrl]
               :action (fn [] (menu/unbind-all-menu-keys))}
      :r      {:title "Reload SpacePhoenix"
               :action (fn []
                         (.reload js/Phoenix))}
      :v      {:title "Video Call"
               :items (video-call)}}))})

(keys/bind "space" ["ctrl"]
           (fn []
             (menu/enter (menu))))


;; Start auto-tile by default
(tile/start-auto-tile)









;; TODO: Create a fast build/watch option