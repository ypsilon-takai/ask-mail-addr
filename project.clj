(defproject ask-mail-addr "1.0.0"
  :description "Retrieve real email addr from LDAP server."
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [javax.servlet/servlet-api "2.5"]
                 [compojure "1.1.8"]
                 [hiccup "1.0.5"]
                 [domina "1.0.2"]
                 [org.clojure/tools.nrepl "0.2.5"]
                 [cider/cider-nrepl "0.8.1"]
                 [org.clojars.pntblnk/clj-ldap "0.0.9"]]
  :source-paths ["src-clj"]
  :cljsbuild {
              :builds [{:source-path "src-cljs"
                        :compiler {:output-to "resources/public/js/query.js"
                                   :optimization :whitespace
                                   :pretty-print true}}]}
  :plugins [[lein-ring "0.8.11"]
            [lein-cljsbuild "1.0.3"]]
  :ring {:handler ask-mail-addr.handler/app
         :init ask-mail-addr.handler/init
         :destroy ask-mail-addr.handler/destroy
         :servlet-path-info? false
         :uberwar-name "askmailaddr.war"
         }
  :resource-paths ["resources"]
  :war-resource-paths ["resources"]
  :profiles
  {:dev {:dependencies [[ring-server "0.3.0"]
                        [ring-mock "0.1.5"]
                        [ring/ring-devel "1.2.0"]]}})
