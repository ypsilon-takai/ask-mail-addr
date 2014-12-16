(ns get-responce
  (:require [ajax.core :refer [POST]]
            [domina :as dom]
            [domina.events :as devt]))

(defn disp-name-and-addr [infos]
  (->> (for [i infos]
         (str (:displayName i) "<" (:emailAddress i) ">"))
       (clojure.string/join "\n" ,,)))

(defn receiver [result]
  (let [text-area (dom/by-id "result-area")
        responce (cljs.reader/read-string result)
        disp-data (disp-name-and-addr responce)]
    (dom/set-value! text-area disp-data)))

(defn submit-info []
  (let [input-data (dom/value (dom/by-id "user-list"))
        param-data (doto (js/FormData.)
                (.append "user-list" input-data))]
    (POST "/mail-addr/ask"
          {:params param-data
           :handler receiver
           :responce-format :edn})
    false))

(defn init []
  (if (and js/document
           (.-getElementById js/document))
    (let [main-form (dom/by-id "main-form")]
      (set! (.-onsubmit main-form) submit-info))))

(set! (.-onload js/window) init)

