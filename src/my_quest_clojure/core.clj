(ns my-quest-clojure.core
  (:require [clojure.string :as str]) ;; подключаем str
  (:import [java.io BufferedReader InputStreamReader]) ;; импорт классов Java
  (:gen-class)) ;; генерируем Java класс для запуска

(defrecord Player [scene inventory])
;; Игрок:
;; - scene: номер текущей сцены
;; - inventory: множество предметов игрока

(defrecord Scene [description actions])
;; Сцена:
;; - description: текст описания сцены
;; - actions: список возможных действий в этой сцене

(defrecord Action [description required-item gives-item next-scene])
;; Действие:
;; - description: текст, что делает игрок
;; - required-item: предмет, который должен быть у игрока для этого действия
;; - gives-item: предмет, который игрок получит после действия
;; - next-scene: номер следующей сцены после действия

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

;; Выводит описание текущей сцены и доступные игроку действия
(defn print-current-scene [player scenario]
  (let [{:keys [scene inventory]} player
        {:keys [description actions]} (get scenario scene)
        available-actions (filter #(or (nil? (:required-item %))
                                       (contains? inventory (:required-item %)))
                                  actions)]
    (println "\n---")
    (println description)
    (if (empty? available-actions)
      (println "Нет доступных действий. Игра окончена.")
      (do
        (println "\nДоступные действия:")
        (doseq [[idx action] (map-indexed vector available-actions)]
          (println (str (inc idx) ". " (:description action))))
        available-actions))))

;; Считывает выбор пользователя из консоли
(defn read-player-choice []
  (print "\nВаш выбор: ")
  (flush)
  (let [reader (BufferedReader. (InputStreamReader. System/in))
        input (.readLine reader)]
    (try
      (Integer/parseInt (str/trim input))
      (catch Exception _ 0))))

;; Обновляет состояние игрока в зависимости от выбранного действия
(defn update-player-state [player scenario choice]
  (let [{:keys [scene inventory]} player
        {:keys [actions]} (get scenario scene)
        available-actions (filter #(or (nil? (:required-item %))
                                       (contains? inventory (:required-item %)))
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
    (let [actions (print-current-scene @player game-scenario)]
      (if (or (nil? actions) (empty? actions))
        (println "\nСпасибо за игру!")
        (do
          (let [choice (read-player-choice)]
            (swap! player update-player-state game-scenario choice)
            (recur)))))))

(defn -main [& args]
  (println "Приключение начинается!")
  (game-loop))
