(ns lazypress.browserid
  (:use [lazypress vmc])
  (:use [clj-http.client :only  [post]])
  (:use [clojure.data.json :only [read-json]]))

(def browserid-verify-url "https://browserid.org/verify")
(def audience
  (if (vmc?)
    "http://lazypress.cloudfoundry.com/"
    "http://localhost:3000/"))

(defn verify [assertion]
  (let [data {:assertion assertion
              :audience audience}
        result (post browserid-verify-url                     
                     {:body (format "assertion=%s&audience=%s"
                                    assertion
                                    audience)
                      :content-type "application/x-www-form-urlencoded"})]
    (read-json (:body result))))

