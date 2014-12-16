(ns ask-mail-addr.routes.mail-addr-picker
  (:require [clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.form :refer :all]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.util :as util]
            [ask-mail-addr.models.ldap :as ldap]))

(defn mail-addr-picker []
  (html5
   [:head
    [:title "Mail address picker"]
    [:meta {:http-equiv "content-type" :content "text/html;charset=utf-8"}]]
   [:body
    "変換したいメール名を入力してください。 ；や改行で区切って複数入力できます。"
    (form-to {:id "main-form"}
             [:post "/mail-addr/ask"]
             (text-area {:id "user-list"
                         :style "width:250px; height:150px;"}
                        :user-list)
             [:br]
             (submit-button {:id "submit"}
                            :submit))
    [:br]
    [:div
     [:p {:id "result-area"
          :name "result-area"}
      "Reselt will be here"]
     (text-area {:id "result-area"
                 :style "width:300px; height:150px;"}
                :result-area)]
    (include-js "public/js/query.js")]))


(defn print-result [info-list]
  (->> (for [[dispname addr] (map (juxt :displayName :emailAddress) info-list)]
         (str dispname "&lt;" addr "&gt;" ))
       (str/join "<br>" ,,)))

(defn get-maddr [user-list]
  (print-result (ldap/ask-all-email-address user-list)))
