(ns spacephoenix.core
  (:require
   [clojure.string :as string]
   [spacephoenix.app :as app]
   [spacephoenix.custom :as custom]
   [spacephoenix.emacs :as emacs]
   [spacephoenix.keys :as keys]
   [spacephoenix.menu :as menu]
   [spacephoenix.message :as message]
   [spacephoenix.screen :as screen]

   [spacephoenix.window.core :as window]
   [spacephoenix.window.move :as window.move]
   [spacephoenix.pseudo-space :as ps]

   [spacephoenix.window.switcher :as window-switcher]))

;;(ps/init)
(message/alert "welcome to spacephoenix")

(defn exit []
  {:title "exit"
   :action menu/unbind-all-menu-keys})

(defn make-menu [base-config]
  (assoc base-config :escape (exit)))

(defn action [title action-fn]
  {:title title
   :action action-fn})

(defn handle-launched-app [app]
  (when app
    (when-let [space (ps/app-space (app/title app))]
      (ps/activate space))))

(defn launch-app [app-name]
  {:title (string/lower-case app-name)
   :action (fn [] (handle-launched-app (app/launch app-name)))})

(defn apps []
  (make-menu
   {:a {:title "alfred"
        :action (fn [] (app/launch "Alfred 5"))}
    :b (launch-app "Firefox")
    :c (launch-app "Calendar")
    :f (launch-app "Finder")
    :i (launch-app "wezterm")
    :m (launch-app "Mail")
    :q (action "quit" app/quit-focused)
    :w (launch-app "Webex")
    :s (launch-app "Safari")
    :z (launch-app "zoom.us")}))

(defn emacs []
  (make-menu
   {:c (action "capture" emacs/capture)}))

(defn window []
  (make-menu
   {:m {:title "move"
        :items
        (make-menu
         {:h (action "west" window.move/west)
          :l (action "east" window.move/east)
          :j (action "south" window.move/south)
          :k (action "north" window.move/north)})}
    :q (action "close" window/close-focused)
    :x (action "list" (fn [] (message/alert (window/title (window/neighbor (window/focused))))))
    }))

(defn spaces []
  (make-menu
   (merge
    (reduce
     (fn [menu space]
       (let [key (keyword (str space))
             title (str "activate space " space)
             act-fn (fn [] (ps/activate space))]
         (assoc menu key (action title act-fn))))
     {}
     (ps/spaces))
    {:m {:title "move"
         :items (reduce
                 (fn [menu space]
                   (let [key (keyword (str space))
                         title (str "move to space " space)
                         act-fn (fn [] (ps/to-space space))]
                     (assoc menu key (action title act-fn))))
                 {}
                 (ps/spaces))}
     :l {:title "list"
         :action ps/space-list}
     :n {:title "new space"
         :action ps/make}})))

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
     {:space (action "launch app"
                     (fn [] (handle-launched-app (app/launch-from-input))))
      :a {:title "apps"
          :items (apps)}
      :e {:title "emacs"
          :items (emacs)}
      :f (action "focus" window-switcher/switch!)
      :r (action "reload spacephoenix" (fn [] (.reload js/Phoenix)))
      :w {:title "window"
          :items (window)}
      :s {:title "space"
          :items (spaces)}
      :z (action "minimize" ps/minimize-focused)
      :g {:title "quit"
          :modifiers [:ctrl]
          :action (fn [] (menu/unbind-all-menu-keys))}}))})

(keys/bind "space" (fn [] (menu/enter (menu))) :modifiers ["ctrl"])
