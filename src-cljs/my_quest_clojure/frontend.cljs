(ns my-quest-clojure.frontend
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [goog.dom :as gdom])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn render-scene [{:keys [description actions]}]
  (let [root (gdom/getElement "app")]
    (set! (.-innerHTML root) "")
    (let [desc-el (.createElement js/document "p")]
      (set! (.-textContent desc-el) description)
      (.appendChild root desc-el))
    (if (empty? actions)
      (let [end-el (.createElement js/document "p")]
        (set! (.-textContent end-el) "Игра окончена. Спасибо за игру!")
        (.appendChild root end-el))
      (do
        (let [form (.createElement js/document "form")]
          (set! (.-id form) "choice-form")
          (doseq [{:keys [idx description]} actions]
            (let [label (.createElement js/document "label")
                  radio (.createElement js/document "input")
                  br (.createElement js/document "br")]
              (set! (.-type radio) "radio")
              (set! (.-name radio) "choice")
              (set! (.-value radio) (str idx))
              (when (= idx 1)
                (set! (.-checked radio) true))
              (.appendChild label radio)
              (.appendChild label (.createTextNode js/document (str " " description)))
              (.appendChild form label)
              (.appendChild form br)))
          (let [submit (.createElement js/document "button")]
            (set! (.-type submit) "submit")
            (set! (.-textContent submit) "Выбрать")
            (.appendChild form submit))
          (.appendChild root form)
          (.addEventListener form "submit"
                             (fn [e]
                               (.preventDefault e)
                               (let [choice (.. form -choice -value)]
                                 (go
                                   (let [resp (<! (http/post "/step"
                                                            {:json-params {:choice choice}}))
                                         data (:body resp)]
                                     (render-scene data)))))))))))

(defn ^:export start []
  (go
    (let [resp (<! (http/get "/scene" {:with-credentials? false}))
          data (:body resp)]
      (render-scene data))))
