(ns lazypress.vmc
  (:require [clojure.data.json :as json]))

(defn get-env [key]
  (System/getenv key))

(defn mongo-config [key]
  (if-let [services (get-env "VCAP_SERVICES")]
    (let [services-dict (json/read-json services false)]
      (-> services-dict
          (get "mongodb-1.8")
          first
          (get "credentials")
          (get key)))))

