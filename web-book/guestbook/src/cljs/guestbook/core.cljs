(ns guestbook.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET POST]]))

(defn home []
  (let [messages (atom nil)]
    (get-messages messages)
    (fn []
      [:div
       [:div.row
        [:div.span12
         [message-list messages]]]
       [:div.row
        [:div.span12
         [message-form messages]]]])))


(defn errors-component [errors id]
  (when-let [error (id @errors)]
    [:div.alert.alert-danger (clojure.string/join error)]))


(defn send-message! [fields errors messages]
  (POST "/messages"
        {:params @fields
         :format :json
         :headers
         {"Accept" "accplication/transit+json"
          "x-csrf-token" (.-value (.getElementById js/document "token"))}
         :handler #(do
                     (reset! errors nil)
                     (swap! messages conj (assoc @fields :timestamp (js/Date.))))

         :error-handler #(do
                           (.log js/console (str (:response %)))
                           ;;(reset! errors (get-in % [:response :errors]))
                           )}))

(defn get-messages [messages]
  (GET "/messages"
       {:headers {"Accept" "application/transit+json"}
        :handler #(reset! messages (vec %))}))

(defn message-form [messages]
  (let [fields (atom {})
        errors (atom {:name "Wait? This works?"
                      :message "This too?"})]
    (fn []
      [:div.content
       [:div.form-group
        [:p "Name:"
         [:input.form-control
          {:type :text
           :name :name
           :on-change #(swap! fields assoc :name (-> % .-target .-value))
           :value (:name @fields)}]]]

       [errors-component errors :name]
       [:p "Message:"
        [:textarea.form-control
         {:rows 4
          :cols 50
          :name :message
          :on-change #(swap! fields assoc :message (-> % .-target .-value))}
         (:message @fields)]]
       [errors-component errors :message]
       [:input.btn.btn-primary
        {:type :submit
         :value "comment"
         :on-click #(send-message! fields errors messages)}]])))

(defn message-list [messages]
  [:ul.content
   (for [{:keys [timestamp message name]} @messages]
     ^{:key timestamp}
     [:li
      [:time (.toLocaleString timestamp)]
      [:p message]
      [:p " - " name]])])

(reagent/render
 [home]
 (.getElementById js/document "content"))
