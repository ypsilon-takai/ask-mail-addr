(ns get-responce
  (:require [goog.net.XhrIo :as xhr]
            [domina :as dom]
            [domina.events :as devt]))

(defn receiver [event]
  (let [responce (.-target event)]
    (.write js/document (.getResponceText responce))))

(defn post [url content]
  (xhr/send url receiver "POST" content))

(defn test-func []
  (let [text-area (dom/by-id "result-area")]
    (dom/set-value! text-area "IT WORKS!")))

(defn init []
  (if (and js/document
           (.-getElementById js/document))
    (let [main-form (.getElementById js/document "main-form")]
      (set! (.-onsubmit main-form) test-func))))

(set! (.-onload js/window) init)
