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
   [spacephoenix.window.core :as window]
   [spacephoenix.window.switcher :as window-switcher]))

(message/alert "welcome to spacephoenix")

(defn exit []
  {:title "exit"
   :action
   (fn [] (menu/unbind-all-menu-keys))})

(defn make-menu [base-config]
  (assoc base-config :escape (exit)))

(def config
  {:switch-space-modifiers [:command]
   :m1? false})

;; Use grammarly config for this?
(defn launch-app [app]
  {:title (string/lower-case app)
   :action (fn [] (app/launch app))})


(defn apps []
  (make-menu
   {:a {:title "alfred"
        :action (fn [] (app/launch "Alfred 5"))}
    :b (launch-app "Firefox")
    :c (launch-app "Calendar")
    :f (launch-app "Finder")
    :i (launch-app "wezterm")
    :m (launch-app "Mail")
    :q {:title "Quit"
        :action app/quit-focused}
    :w (launch-app "Webex")
    :s (launch-app "Safari")
    :z (launch-app "zoom.us")}))

(defn emacs []
  (make-menu
   {:c {:title "capture"
        :action (fn [] (emacs/capture))}
    :e {:title "emacs-anywhere"
        :action (fn [] (proc/emacs-anywhere))}}))

(defn machine []
  (make-menu
   {:s {:title "sleep"
        :action (fn [] (proc/sleep))}}))

(defn move-window  []
  (let [space-count (count (space/all))
        space-numbers (range 1 (inc space-count))]
    (make-menu
     (reduce
      (fn [result number]
        (let [number-string (str number)]
              (assoc result (keyword number-string)
                     {:title (str "send window to space " number-string)
                      :action
                      (fn [] (space/send-focused-window-to-space number))})))
      {}
      space-numbers))))

(defn windows []
  (let [initial-map {:f {:title "maximize"
                         :action window/maximize-focused}
                     :l {:title "list"
                         :action (fn [] (message/alert
                                        (mapv
                                         (fn [window]
                                           (str (window/title window) "\n"))
                                         (window/all))
                                        :duration 10))}


                     :m {:title "move"
                         :items (move-window)}
                     :z {:title "minimize"
                         :action window/minimize-focused}
                     :q {:title "close"
                         :action window/close-focused}}
        space-count (count (space/all))
        space-numbers  (range 1 (inc space-count))]
    (make-menu
     (reduce
      (fn [result number]
        (let [number-string (str number)]
          (assoc result (keyword number-string)
                 {:title (str "follow window to space " number-string)
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
     {:nil {:title (str "switch to space: "
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
     {:space {:title "alfred"
              :action
              (fn [] (app/launch "Alfred 5"))}
      :a      {:title "apps"
               :items (apps)}
      :e      {:title "emacs"
               :items (emacs)}
      :o      {:title "operation"
               :items (machine)}
      :r      {:title "reload spacephoenix"
               :action (fn []
                         (.reload js/Phoenix))}
      :w      {:title "window"
               :items (windows)}
      :s      {:title "Space"
               :items {:s {:title "start auto tile"
                           :action (fn [] (tile/start-auto-tile))}
                       :q {:title "stop auto tile"
                           :action (fn [] (tile/stop-auto-tile))}
                       :t {:title "tile"
                           :action (fn [] (tile/tile))}}}
      :x {:title "switch window"
          :action (fn [] (window-switcher/switch!))}
      :g      {:title "quit"
               :modifiers [:ctrl]
               :action (fn [] (menu/unbind-all-menu-keys))}}))})

(keys/bind "space"
           (fn []
             (menu/enter (menu)))
           :modifiers ["ctrl"])


;; Start auto-tile by default
(tile/start-auto-tile)

;; TODO: Create a fast build/watch option
