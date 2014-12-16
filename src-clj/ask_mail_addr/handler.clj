(ns ask-mail-addr.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ask-mail-addr.routes.mail-addr-picker :as maddr]
            [clojure.tools.nrepl.server :as nrepl]
            [cider.nrepl :refer (cider-nrepl-handler)]))

(def nrepl-server (atom nil))

(defn init []
  (println "Starting mail address getter.")
  (reset! nrepl-server (nrepl/start-server :port 7888 :handler cider-nrepl-handler)))

(defn destroy []
  (println "End.")
  (nrepl/stop-server @nrepl-server))

(defroutes app-routes
  (GET "/mail-addr" [] (maddr/mail-addr-picker))
  (POST "/mail-addr/ask" [user-list]
        (maddr/get-maddr user-list))
  (route/resources "/public/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

