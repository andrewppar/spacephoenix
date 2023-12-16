(ns spacephoenix.app)

(defn launch [app-name & {:keys [focus?] :or {focus? true}}]
  (.launch js/App app-name (clj->js {:focus focus?})))

(defn quit-focused [& {:keys [force?] :or {force? false}}]
  (.terminate (.focused js/App) (clj->js {:force force?})))

(defn focus [app-name]
  (.focus (.get js/App app-name)))


(def untiled-apps
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

(defn all [& {:keys [tiled-only?] :or {tiled-only? true}}]
  (let [all-apps (.all js/App)]
    (if tiled-only?
      (filter
       (fn [app]
         (not (contains? untiled-apps (.name app))))
       all-apps)
      all-apps)))
