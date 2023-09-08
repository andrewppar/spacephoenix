var Phoenix = function() {}
// static
Phoenix.reload = function() {}
Phoenix.set = function(preferences) {}
Phoenix.log = function(message) {}
Phoenix.notify = function(message) {}

var App = function() {}
//static
App.get = function(appName) {}
App.launch = function(appName, optionals) {}
App.focused = function() {}
App.all = function() {}

App.processIdentifier = function() {}
App.bundleIdentifier = function() {}
App.name = function() {}
App.isActive = function() {}
App.isHidden = function() {}
App.isTerminated = function() {}
App.mainWindow = function() {}
App.windows = function(optionals) {}
App.activate = function() {}
App.focus = function() {}
App.show = function() {}
App.hide = function() {}
App.terminate = function(optionals) {}

var Event
Event.on = function(event,callback) {}
Event.once = function(event,callback) {}
Event.off = function(identifier) {}

var Key = function() {}
Key.on = function(key, modifiers, callback) {}
Key.off = function(handler) {}

var Model = function() {}
// properties
Model.origin = function() {}
Model.duration = function() {}
Model.text = function() {}

Model.frame = function() {}
Model.show = function() {}
Model.close = function() {}

var Rectangle
//properties
Rectangle.x = function() {}
Rectangle.y = function() {}
Rectangle.width = function() {}
Rectangle.height = function() {}

var Screen = function() {}
Screen.frameInRectangle = function() {}
Screen.flippedVisibleFrame = function() {}
Screen.main = function() {}
Screen.next = function() {}
Screen.previous = function() {}
Screen.all = function() {}
Screen.windows = function() {}
Screen.currentSpace = function() {}
Screen.spaces = function() {}
Screen.frame = function () {}

var Space = function () {}
Space.moveWindows = function(windows) {}
Space.all = function () {}
Space.active = function () {}
Space.next = function () {}
Space.windows = function () {}
Space.hash    = function () {}

var Task
Task.run = function(path, args, callback) {}
Task.terminate = function(int) {}
Task.status = function() {}
Task.output = function() {}
Task.error = function () {}

var Timer
Timer.after = function(seconds, callback) {}
Timer.every = function(seconds, callback) {}
Timer.off   = function(identifier) {}

var Window = function() {}
// static
Window.focused = function() {}
Window.at = function(point) {}
Window.all = function(optionals) {}
Window.recent = function() {}

// Members
Window.others = function(optional) {}
Window.title = function() {}
Window.isMain = function() {}
Window.isNormal = function() {}
Window.isFullScreen = function() {}
Window.isMinimised = function() {}
Window.isMinimized = function() {}
Window.isVisible = function() {}
Window.subrole = function() {}

Window.frame = function() {}
Window.topLeft = function() {}
Window.size = function() {}
// Rect frame
Window.setFrame = function(frame) {}
// Point point
Window.setTopLeft = function(point) {}
// Size size
Window.setSize = function(size) {}
Window.maximize = function() {}
Window.minimize = function() {}
Window.unminimize = function() {}
Window.screen = function() {}
Window.spaces = function () {}
Window.app = function() {}
Window.focus = function() {}
Window.focusClosestNeighbor = function() {}
Window.close = function () {}
Window.neighbors = function() {}
