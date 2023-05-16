(ns loading_button
  (:require [archstone.catalyst.components.api :as comps]
            [archstone.catalyst.core :refer :all]
            [archstone.catalyst.globals.flutter.nubank.http :as flutter.http]
            [archstone.catalyst.globals.flutter.nuvigator :as nuvigator]
            [archstone.catalyst.globals.flutter.riverpod :as flutter.riverpod]
            [archstone.catalyst.globals.std.collections :as std.coll]))


(def request
  (-> (flutter.http/|request
       "http://localhost:8080/mock/severino/api/challenge/1/confirm/success"
       {:method  flutter.http/method-get
        :headers {:content-type "application/json"}})
      (std.coll/|match
       {"ok"    (|fn [_] :challenge-started)
        "error" (|fn [_] :error)}
       (|fn [_] :error))))

(defn screen [_ _]
  (|let [http-result* (flutter.riverpod/|create-cell)
         is-loading*  (flutter.riverpod/|create-cell false)]
        (comps/|riverpod-consumer
         {:build (|fn [watch]
                      (comps/|bottom-bar
                       {:primary (comps/|button
                                  {:child      (comps/|text {:label "Request"})
                                   :on-pressed (|fn []
                                                    (flutter.riverpod/|set is-loading* true)
                                                    (flutter.riverpod/|set http-result* request)
                                                    (nuvigator/|open (nuvigator/|deeplink "nuapp://bdc/nu-bdc-sandbox/expr/success")
                                                                     {:push-method (nuvigator/|push-method :push-replacement)}))
                                   :loading    (|$ watch is-loading*)})}))})))
