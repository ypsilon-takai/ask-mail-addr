(ns ask-mail-addr.models.ldap
  (:require [clj-ldap.client :as ldap]
            [clojure.string :as str])
  (:import [com.unboundid.ldap.sdk.Filter]))

(def ldap-info {:host "ldap.host.name"
                    :bind-dn "bind-dn"
                    :password "yourpassword"
                    :base-dn "base-dn"})

(def ldap-connection (ldap/connect ldap-info))

(defn- ask-real-email-address [dispname]
  (let [dispname-filter (com.unboundid.ldap.sdk.Filter/createEqualityFilter "displayName" dispname)]
    (if-let [res (-> (ldap/search ldap-connection
                                   (:base-dn ldap-info)
                                   {:attributes [:displayName :emailAddress]
                                    :filter dispname-filter})
                      (first ,,)
                      (dissoc ,, :dn))]
      res
      {:displayName dispname
       :nsnPrimaryEmailAddress "== NOT FOUND =="})))


(defn ask-all-email-address [dispnames]
  (let [dispname-list (map str/trim (str/split dispnames #"[;\n\r]+"))]
    (map ask-real-email-address dispname-list)))

