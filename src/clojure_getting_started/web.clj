(ns clojure-getting-started.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [resource-response response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [vindvarsler.core :as vv]))

(defroutes app-routes
  (GET "/" [] (response {:kuling (vv/gale?)}))
  (route/not-found "Not found"))

(def app
  (-> app-routes
      (middleware/wrap-json-response)
      (middleware/wrap-json-body)
      (wrap-defaults api-defaults)))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
