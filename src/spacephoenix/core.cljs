(ns spacephoenix.core
  (:require
   [clojure.string :as string]
   [spacephoenix.app :as app]
   [spacephoenix.config :as config]
   [spacephoenix.emacs :as emacs]
   [spacephoenix.keys :as keys]
   [spacephoenix.menu :as menu]
   [spacephoenix.message :as message]
   [spacephoenix.pseudo-space :as ps]
   [spacephoenix.tile :as tile]
   [spacephoenix.window.core :as window]
   [spacephoenix.window.screen :as window.screen]
   [spacephoenix.window.switcher :as window-switcher]))

(when (config/auto-tile)
  (tile/start-auto-tile))
(message/alert "welcome to spacephoenix")

(defn exit []
  {:title "exit"
   :action menu/unbind-all-menu-keys})

(defn make-menu [base-config]
  (assoc base-config :escape (exit)))

(defn action [title action-fn]
  {:title title
   :action action-fn})

(defn launch-app [app-name]
  {:title (string/lower-case app-name)
   :action (fn [] (app/launch app-name))})

(defn apps []
  (make-menu
   {:b {:title "browser"
        :items (make-menu
                {:b (launch-app "Zen Browser")
                 :f (launch-app "Firefox")
                 :z (launch-app "Zen Browser")})}
    :c (launch-app "Calendar")
    :f (launch-app "Finder")
    :i (launch-app "wezterm")
    :m (launch-app "Mail")
    :q (action "quit" app/quit-focused)
    :w (launch-app "Webex")
    :z (launch-app "zoom.us")}))

(defn emacs []
  (make-menu
   {:c (action "capture" emacs/capture)}))

(defn window []
  (make-menu
   {:q (action "close" window/close-focused)}))

(defn make-space-menu [title-fn action-fn]
  (make-menu
   (reduce
    (fn [menu space]
      (let [key (keyword (str space))
            title (title-fn space)
            act-closure (partial action-fn space)]
        (assoc menu key (action title act-closure))))
    {}
    (ps/spaces))))

(defn space []
  (make-menu
   {:f {:title "move and follow"
        :items (make-space-menu
                (fn [space] (str "move and follow to " space))
                (fn [space] (ps/to-space space) (ps/activate space)))}
    :d (action "remove from space"
               (fn [] (ps/remove-window! (window/focused))))
    :l (action "list" ps/space-list)
    :m {:title "move"
        :items (make-space-menu
                (fn [space] (str "move to space " space))
                (fn [space] (ps/to-space space)))}
    :n (action "new space" ps/make)
    :q (action "stop auto tile" tile/stop-auto-tile)
    :r (action "reset spaces" ps/reassign!)
    :s (action "start auto tile" tile/start-auto-tile)
    :t (action "tile" tile/tile)
    :x {:title "delete"
        :items (make-space-menu
                (fn [space] (str "delete space " space))
                (fn [space] (ps/delete! space)))}}))

(defn menu []
  {:title "Menu"
   :items
   (merge
    (make-space-menu
     (fn [space] (str "activate space " space))
     (fn [space] (ps/activate space)))
    (make-menu
     {:space (launch-app "quicksilver")
      :a {:title "apps"
          :items (apps)}
      :e {:title "emacs"
          :items (emacs)}
      :f (action "focus" window-switcher/switch!)
      :m (action "to screen" window.screen/to-screen!)
      :r (action "reload spacephoenix" (fn [] (.reload js/Phoenix)))
      :w {:title "window"
          :items (window)}
      :s {:title "space"
          :items (space)}
      :z (action "minimize" ps/minimize-focused)
      :g {:title "quit"
          :modifiers [:ctrl]
          :action (fn [] (menu/unbind-all-menu-keys))}}))})

(keys/bind "space" (fn [] (menu/enter (menu))) :modifiers ["ctrl"])
