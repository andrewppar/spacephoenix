(ns spacephoenix.core
  (:require
   [clojure.string :as string]
   [spacephoenix.app :as app]
   [spacephoenix.custom :as custom]
   [spacephoenix.keys :as keys]
   [spacephoenix.menu :as menu]
   [spacephoenix.message :as message]
   [spacephoenix.process :as proc]
   [spacephoenix.space :as space]
   [spacephoenix.tile :as tile]
   [spacephoenix.emacs :as emacs]
   [spacephoenix.window :as window]))

(message/alert "Welcome to SpacePhoenix")

(defn exit []
  {:title "Exit"
   :action
   (fn [] (menu/unbind-all-menu-keys))})

(defn make-menu [base-config]
  (assoc base-config :escape (exit)))

(def config
  {:switch-space-modifiers [:command]
   :m1? false})

;; Use grammarly config for this?
(defn launch-app [app]
  {:title app
   :action (fn [] (app/launch app))})


(defn apps []
  (make-menu
   {:a {:title "Alfred"
        :action (fn [] (app/launch "Alfred 5"))}
    :b (launch-app "Brave Browser")
    :c (launch-app "Calendar")
    :f (launch-app "Finder")
    :i (launch-app "iTerm")
    :m (launch-app "Mail")
    :q {:title "Quit"
        :action app/quit-focused}
    :w (launch-app "Webex")
    :s (launch-app "Safari")
    :z (launch-app "Zoom")}))

(defn emacs []
  (make-menu
   {:c {:title "Capture"
        :action (fn [] (emacs/capture))}}))

(defn machine []
  (make-menu
   {:s {:title "Sleep"
        :action (fn [] (proc/sleep))}}))

(defn move-window  []
  (let [space-count (count (space/all))
        space-numbers (range 1 (inc space-count))]
    (make-menu
     (reduce
      (fn [result number]
        (let [number-string (str number)]
              (assoc result (keyword number-string)
                     {:title (str "Send App to Space " number-string)
                      :action
                      (fn [] (space/send-focused-window-to-space number))})))
      {}
      space-numbers))))

(defn windows []
  (let [initial-map {:f {:title "Maximize"
                         :action window/maximize-focused}
                     :m {:title "Move"
                         :items (move-window)}
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
                  (fn [] (space/send-focused-window-to-space-and-refocus number))})))
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

(defn menu []
  {:title "Menu"
   :items
   (make-menu
    (merge
     (switch-space-bindings)
     (reduce-kv
      (fn [custom-menu k submenu]
        (assoc custom-menu k
               (update submenu :items make-menu)))
      {}
      custom/menu)
     {:space {:title "Alfred"
              :action
              (fn [] (app/launch "Alfred 5"))}
      :a      {:title "Apps"
               :items (apps)}
      :e      {:title "Emacs"
               :items (emacs)}
      :o      {:title "Operation"
               :items (machine)}
      :r      {:title "Reload SpacePhoenix"
               :action (fn []
                         (.reload js/Phoenix))}
      :t      {:title "Tile"
               :action (fn [] (tile/tile))}
      :w      {:title "Window"
               :items (windows)}
      :s      {:title "Space"
               :items {:s {:title "Start Auto Tile"
                           :action (fn [] (tile/start-auto-tile))}
                       :q {:title "Stop Auto Tile"
                           :action (fn [] (tile/stop-auto-tile))}}}
      :g      {:title "Quit"
               :modifiers [:ctrl]
               :action (fn [] (menu/unbind-all-menu-keys))}}))})

(keys/bind "space" ["ctrl"]
           (fn []
             (menu/enter (menu))))


;; Start auto-tile by default
(tile/start-auto-tile)

;; TODO: Create a fast build/watch option
