(ns tic-tac-toe.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css include-js html5]]))

(defpartial layout [& content]
            (html5
              [:head
               [:title "tic-tac-toe"]
               [:link {:type "text/css", :href "/css/bootstrap.css", :rel "stylesheet", :media "screen"}]
               [:link {:type "text/css", :href "/css/bootstrap-responsive.css", :rel "stylesheet", :media "screen"}]
               [:link {:type "text/css", :href "/css/app.css" :rel "stylesheet"}]]
              [:body
               [:div#wrapper
                content]
                (include-js "/js/jquery-1.8.3.min.js" "/js/bootstrap.min.js" "/js/cljs.js")]))
