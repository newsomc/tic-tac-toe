(defproject tic-tac-toe "0.1.0-SNAPSHOT"
            :description "FIXME: write this!"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [noir "1.3.0-beta3"]
                           [jayq "1.0.0"]]
            :plugins [[lein-cljsbuild "0.2.9"]]
            :cljsbuild 
            {
            	:source-path "src-cljs/tic-tac-toe"
            	:compiler
            	{
            		:output-to "resources/public/js/cljs.js"
            		:optimizations :simple
              		:pretty-print true
            	}
            }
            :main tic-tac-toe.server)

