(ns spacephoenix.process)

(defn applescript [script]
  (let [task (.run js/Task
                   "/usr/bin/osascript"
                   (clj->js ["-e" script])
                   identity)]
    task))

(defn browse-url [url]
;;; (valid-url? url)
  (.run js/Task "/usr/bin/open" (clj->js [url]) identity))

(defn sleep []
  (.run js/Task "/usr/bin/pmset" (clj->js ["sleepnow"]) identity))

(defn capture [task-description]
  (let [command (str "(org-capture-engine-issue \""
                     task-description
                     "\")")
        emacsclient "/run/current-system/sw/bin/emacsclient"]
    (.run js/Task emacsclient (clj->js ["--eval" command]))))
