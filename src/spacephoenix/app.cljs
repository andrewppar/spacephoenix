(ns spacephoenix.app
  (:require [spacephoenix.screen :as screen]))

(defn launch [app-name & {:keys [focus?] :or {focus? true}}]
  (.launch js/App app-name (clj->js {:focus focus?})))

(defn quit-focused [& {:keys [force?] :or {force? false}}]
  (.terminate (.focused js/App) (clj->js {:force force?})))

(defn focus [app-name]
  (.focus (.get js/App app-name)))

(defn windows [app]
  (.windows app))

(def dont-care-apps
  #{"loginwindow"
    "universalaccessd"
    "WindowManager"
    "Notification Center"
    "Dock"
    "SystemUIServer"
    "Control Center"
    "ViewBridgeAuxiliary"
    "Wallpaper"
    "Wi-Fi"
    "Spotlight"
    "ARDAgent"
    "SSMenuAgent"
    "Dock Extra"
    "CursorUIViewService"
    "com.apple.PressAndHold"
    "Webex Networking"
    "WebexHelper"
    "AirPlayUIAgent"
    "TextInputMenuAgent"
    "Phoenix (Debug)"
    "1Password Browser Helper"
    "Tunnelblick"
    "Homerow"
    "Secure Endpoint Connector"
    "Duo Desktop"
    "open"
    "universalAccessAuthWarn"
    "Mail Networking"
    "Mail Graphics and Media"
    "Password AutoFill (Tunnelblick)"
    "Mail Web Content"
    "UserNotificationCenter"
    "UIKitSystem"
    "chronod"
    "washost"
    "com.apple.CoreSimulator.CoreSimulatorService"
    "Simulator"
    "CoreDeviceService"
    "Open and Save Panel Service (Xcode)"
    "QuickLookUIService (Open and Save Panel Service (Xcode))"
    "Keychain Circle Notification"
    "CoreLocationAgent"
    "ThemeWidgetControlViewService (Xcode)"
    "ThemeWidgetControlViewService (Console)"})

(defn title [app]
  (.name app))

(defn all [& {:keys [tiled-only?] :or {tiled-only? true}}]
  (let [all-apps (.all js/App)]
    (if tiled-only?
      (filter
       (fn [app]
         (not (contains? dont-care-apps (title app))))
       all-apps)
      all-apps)))

(defn launch-from-input []
  (let [{:keys [height width]} (screen/current-size-and-position)
        modal (js/Modal.)]
    (set! (.-isInput modal) (clj->js true))
    (set! (.-appearance modal) (clj->js "dark"))
    (set! (.-origin modal) (clj->js {"x" (/ width 2)
                                     "y" (/ height 2)}))
    (set! (.-textDidCommit modal) (clj->js
                                   (fn [value _]
                                     (launch value)
                                     (.close modal))))
    (.show modal)))
