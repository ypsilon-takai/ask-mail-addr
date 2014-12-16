(ns ask-mail-addr.routes.mail-addr-picker
  (:require [clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.form :refer :all]
            [hiccup.page :refer [html5 include-css]]
            [hiccup.util :as util]
            [ask-mail-addr.models.ldap :as ldap]))

(defn mail-addr-picker []
  (html5
   [:head
    [:title "Mail address picker"]
    [:meta {:http-equiv "content-type" :content "text/html;charset=utf-8"}]]
   [:body
    "変換したいメール名を入力してください。 ；で区切れば、複数入力できます。"
    (form-to [:post "/mail-addr/ask"]
             (text-area {:style "width:250px; height:150px;"} "user-list")
             [:br]
             (submit-button :submit))]))


(defn print-result [info-list]
  (->> (for [[dispname addr] (map (juxt :displayName :emailAddress) info-list)]
         (str dispname "&lt;" addr "&gt;" ))
       (str/join "<br>" ,,)))

(defn get-maddr [user-list]
  (print-result (ldap/ask-all-email-address user-list)))


