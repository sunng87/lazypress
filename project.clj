(defproject lazypress "0.1.0-SNAPSHOT"
            :description "Simple online writter"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [compojure "1.0.0-RC2"]
                           [clj-markdown "0.1.0"]
                           [congomongo "0.1.7"]]
            :dev-dependencies [[lein-ring "0.4.3"]]
            :ring {:handler lazypress.app/app})

