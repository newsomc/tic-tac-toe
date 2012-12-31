(ns tic-tac-toe.server
  (:require [noir.server :as server]))

(server/load-views-ns 'tic-tac-toe.views)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'tic-tac-toe})))

