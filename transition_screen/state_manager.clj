(ns transition-screen.state-manager
  (:require [archstone.catalyst.components.api :as comps]
            [archstone.catalyst.components.props.api :as props]
            [archstone.catalyst.core :refer :all]
            [archstone.catalyst.globals.flutter :as flutter]
            [archstone.catalyst.globals.flutter.futures :as futures]
            [archstone.catalyst.globals.flutter.nubank.http :as flutter.http]
            [archstone.catalyst.globals.flutter.nuvigator :as nuvigator]
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

(def feedback-screen
  {:user-confirmed "nuapp://bdc/nu-bdc-sandbox/expr/confirmed"
   :user-denied    "nuapp://bdc/nu-bdc-sandbox/expr/denied"
   :error          "nuapp://bdc/nu-bdc-sandbox/expr/error"})

(defn screen [_ _]
  (comps/|state-manager
   {:initial-state         :challenge-started
    :on-initial-async-task (|fn [update-state] (|$ update-state request))
    :build                 (|fn [_ current-state]
                                (comps/|nuds-screen
                                 {:analytics-context (props/|analytics-context "ok" "ok")
                                  :body              (comps/|transition-screen
                                                      {:semantics-label    (props/|rosetta-plain-string "Transition Screen")
                                                       :on-transitions-end (|fn []
                                                                                (nuvigator/|open (nuvigator/|deeplink (|lookup feedback-screen current-state))
                                                                                                 {:push-method (nuvigator/|push-method :push-replacement)}))
                                                       :on-error-builder   (|fn [_ _] true)
                                                       :steps              [(comps/|transition-screen|step
                                                                             (props/|rosetta-plain-string "step 1")
                                                                             (|fn [] (futures/|delayed 1500N)))
                                                                            (comps/|transition-screen|step
                                                                             (props/|rosetta-plain-string "step 2")
                                                                             (|fn [] (futures/|delayed 1500N)))
                                                                            (comps/|transition-screen|step
                                                                             (props/|rosetta-plain-string "step 3")
                                                                             (|fn [] (futures/|delayed 1500N)))]})}))}))
