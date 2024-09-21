(ns spacephoenix.core
  (:require
   [clojure.string :as string]
   [spacephoenix.app :as app]
   [spacephoenix.custom :as custom]
   [spacephoenix.emacs :as emacs]
   [spacephoenix.keys :as keys]
   [spacephoenix.menu :as menu]
   [spacephoenix.message :as message]
   [spacephoenix.process :as proc]))

(message/alert "welcome to spacephoenix")
;;(message/alert (.run js/Task "which" (clj->js ["emacsclient"]) identity))

(defn exit []
  {:title "exit"
   :action
   (fn [] (menu/unbind-all-menu-keys))})

(defn make-menu [base-config]
  (assoc base-config :escape (exit)))

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
        :action (fn [] (emacs/capture))}}))

(defn menu []
  {:title "Menu"
   :items
   (make-menu
    (merge
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
      :r      {:title "reload spacephoenix"
               :action (fn []
                         (.reload js/Phoenix))}
      :g      {:title "quit"
               :modifiers [:ctrl]
               :action (fn [] (menu/unbind-all-menu-keys))}}))})

(keys/bind "space"
           (fn []
             (menu/enter (menu)))
           :modifiers ["ctrl"])
