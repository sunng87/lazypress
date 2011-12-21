(defproject lazypress "0.1.0-SNAPSHOT"
            :description "Simple online writter"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [compojure "1.0.0-RC2"]
                           [org.clojure/data.json "0.1.1"]
                           [clj-markdown "0.1.0"]
                           [congomongo "0.1.7"]
                           [enlive "1.0.0"]]
            :dev-dependencies [[lein-ring "0.5.2"]]
            :ring {:handler lazypress.app/app
                   :init lazypress.app/app-init})

