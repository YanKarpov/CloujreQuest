(ns my-quest-clojure.core
  (:require [clojure.string :as str])
  (:import [java.io BufferedReader InputStreamReader])
  (:gen-class))

(defrecord Player [scene inventory])

(defrecord Scene [description actions])

(defrecord Action [description required-item gives-item next-scene])

(def game-scenario
  {1 (->Scene
       "Вы проснулись в лесу у подножия старой горы. Рядом лежит крепкая палка, которая может пригодиться."
       [(->Action "Взять палку" nil :палка 2)])

   2 (->Scene
       "Перед вами тропинка раздваивается. Справа слышен шум реки, слева густой лес."
       [(->Action "Пойти к реке" nil nil 3)
        (->Action "Пойти в лес" nil nil 4)])

   3 (->Scene
       "У реки вы нашли заброшенную лодку. Вода кажется холодной и быстрой."
       [(->Action "Взять лодку" nil :лодка 3)
        (->Action "Переплыть реку" :лодка nil 5)
        (->Action "Вернуться к развилке" nil nil 2)])

   4 (->Scene
       "Вы углубились в лес и обнаружили древнюю хижину с запертой дверью."
       [(->Action "Осмотреться вокруг" nil nil 6)
        (->Action "Вернуться к развилке" nil nil 2)
        (->Action "Попробовать открыть дверь" :ключ nil 7)])

   5 (->Scene
       "Вы переплыли реку и оказались у маленькой деревни. Вы спасены!"
       [])

   6 (->Scene
       "Осмотрев окрестности, вы нашли старый ржавый ключ, лежащий под камнем."
       [(->Action "Взять ключ" nil :ключ 4)
        (->Action "Вернуться в хижину" nil nil 4)])

   7 (->Scene
       "Дверь открылась, и перед вами предстает мудрый старец. Он помогает вам выбраться из леса. Конец истории."
       [])})

(def player (atom (->Player 1 #{})))

(defn print-current-scene [player scenario]
  (let [{:keys [scene inventory]} player
        {:keys [description actions]} (get scenario scene)
        available-actions (filter #(and
                                    (or (nil? (:required-item %))
                                        (contains? inventory (:required-item %)))
                                    ;; не показывать действие, если даёт предмет, который уже есть
                                    (not (and (:gives-item %)
                                              (contains? inventory (:gives-item %)))))
                                  actions)]
    (println "\n---")
    (println description)
    (if (empty? available-actions)
      (do
        (println "Нет доступных действий. Игра окончена.")
        nil)
      (do
        (println "\nДоступные действия:")
        (doseq [[idx action] (map-indexed vector available-actions)]
          (println (str (inc idx) ". " (:description action))))
        available-actions))))

(defn read-player-choice []
  (print "\nВаш выбор: ")
  (flush)
  (let [reader (BufferedReader. (InputStreamReader. System/in))
        input (.readLine reader)]
    (try
      (Integer/parseInt (str/trim input))
      (catch Exception _ 0))))

(defn update-player-state [player scenario choice]
  (let [{:keys [scene inventory]} player
        {:keys [actions]} (get scenario scene)
        available-actions (filter #(and
                                    (or (nil? (:required-item %))
                                        (contains? inventory (:required-item %)))
                                    (not (and (:gives-item %)
                                              (contains? inventory (:gives-item %)))))
                                  actions)
        idx (dec choice)
        action (nth available-actions idx nil)]
    (if (and action
             (or (nil? (:required-item action))
                 (contains? inventory (:required-item action))))
      (let [new-inv (if (:gives-item action)
                      (conj inventory (:gives-item action))
                      inventory)
            new-scene (:next-scene action)]
        (->Player new-scene new-inv))
      (do
        (println "Неверный выбор, попробуйте снова.")
        player))))

(defn game-loop []
  (loop []
    (let [actions (print-current-scene @player game-scenario)
          {:keys [scene]} @player]
      (cond
        (or (nil? actions) (empty? actions))
        (println "\nСпасибо за игру!")

        (#{5 7} scene)
        (println "\nПоздравляем! Игра окончена.")

        :else
        (do
          (let [choice (read-player-choice)]
            (swap! player update-player-state game-scenario choice)
            (recur)))))))

(defn -main [& args]
  (println "Приключение начинается!")
  (game-loop))
