(defproject lazypress "0.1.0-SNAPSHOT"
            :description "Simple online writter"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [compojure "1.0.0"]
                           [org.clojure/data.json "0.1.1"]
                           [com.madgag/markdownj-core "0.4.1"]
                           [congomongo "0.1.7"]
                           [enlive "1.0.0"]
                           [commons-codec "1.6"]]
            :dev-dependencies [[lein-ring "0.5.2"]]
            :ring {:handler lazypress.app/app
                   :init lazypress.app/app-init})

