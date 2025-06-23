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
       "Перед вами тропинка раздваивается. Справа слышен шум реки, слева — густой лес."
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
  ;; TODO: вывести описание сцены и список доступных действий
  )

;; Считывает выбор пользователя из консоли
(defn read-player-choice []
  ;; TODO: читать ввод пользователя и возвращать номер выбранного действия
  )

;; Обновляет состояние игрока в зависимости от выбранного действия
(defn update-player-state [player scenario choice]
  ;; TODO: изменить игрока (сцену, инвентарь) согласно действию choice
  )

(defn game-loop []
  (loop []
    (print-current-scene @player game-scenario)
    (let [choice (read-player-choice)]
      (swap! player update-player-state game-scenario choice)
      ;; TODO: добавить условие выхода из игры, например, если нет действий
      (recur))))

(defn -main [& args]
  (println "Приключение начинается!")
  (game-loop))



