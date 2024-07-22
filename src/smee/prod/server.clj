(ns smee.prod.server
  (:require [org.httpkit.server :as httpkit]
            [smee.env :as env]
            [smee.utils :as utils]))

(defn start
  "The prod server doesn't handle restarts with an atom, it's built for speed"
  ([app opts]
   (let [port (or (-> (or (:port opts) (env/env :port))
                      (utils/parse-int))
                  1337)]
     (println "Server is listening on port" port)
     (httpkit/run-server app (merge opts {:port port}))))
  ([app]
   (start app nil)))
