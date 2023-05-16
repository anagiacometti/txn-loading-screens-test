(ns transition-screen.riverpod-consumer
  (:require [archstone.catalyst.components.api :as comps]
            [archstone.catalyst.components.props.api :as props]
            [archstone.catalyst.core :refer :all]
            [archstone.catalyst.globals.flutter.futures :as futures]
            [archstone.catalyst.globals.flutter.nubank.http :as flutter.http]
            [archstone.catalyst.globals.flutter.nuvigator :as nuvigator]
            [archstone.catalyst.globals.flutter.riverpod :as riverpod]
            [archstone.catalyst.globals.std.collections :as std.coll]
            [archstone.catalyst.templates.api :as templates]
            [archstone.schemata.context.authenticated :as schemata.context]))

(defn ^:private transition-delay [] (futures/|delayed 1500N))

(def request
  (-> (flutter.http/|request
       "http://localhost:8080/mock/severino/api/challenge/1/confirm/success"
       {:method  flutter.http/method-get
        :headers {:content-type "application/json"}})
      (std.coll/|match
       {"ok"    (|fn [_] :user-confirmed)
        "error" (|fn [_] :error)}
       (|fn [_] :error))))

(def deeplink
  {:challenge-started "nuapp://bdc/nu-bdc-sandbox/expr/timeout"
   :user-confirmed    "nuapp://bdc/nu-bdc-sandbox/expr/success"
   :error             "nuapp://bdc/nu-bdc-sandbox/expr/error"})

(defn transition-screen [state* response*]
  (comps/|transition-screen
   {:semantics-label    (props/|rosetta-plain-string "Transition Screen")
    :on-transitions-end (|fn []
                             (|let [response (|$ state* response*)
                                    deeplink (|lookup deeplink response)]
                                   (nuvigator/|open (nuvigator/|deeplink deeplink)
                                                    {:push-method (nuvigator/|push-method :pop-and-push)})))
    :on-error-builder   (|fn [_ _]
                             (nuvigator/|pop)
                             (nuvigator/|open (nuvigator/|deeplink "bdc/cancun/expr/bucket-redemption-fail")
                                              {:push-method (nuvigator/|push-method :push-replacement)}))
    :steps              [(comps/|transition-screen|step
                          (props/|rosetta-plain-string "step 1")
                          (|fn [] (riverpod/|set response* request)))
                         (comps/|transition-screen|step
                          (props/|rosetta-plain-string "step 2")
                          (|fn [] (transition-delay)))
                         (comps/|transition-screen|step
                          (props/|rosetta-plain-string "step 3")
                          (|fn [] (transition-delay)))]}))

(defn render-transition-screen [_ _]
  (comps/|will-pop-scope
   {:on-will-pop (|fn [] ())
    :child       (comps/|riverpod-consumer
                  {:build (|fn [watch]
                               (|let [response* (riverpod/|create-cell nil)]
                                     (comps/|nuds-screen {:analytics-context (props/|analytics-context "test" "test")
                                                          :body              (transition-screen watch response*)})))})}))
(defn pre-transition-screen [_ _]
  (templates/|prompt-takeover
   {:analytics-context (props/|analytics-context "fraud-confirmed-virtual-card" "butterfree")
    :app-bar           (comps/|top-bar {:title nil})
    :title             (comps/|nuds-text {:label (props/|rosetta-plain-string "É você que está fazendo essa compra?")})
    :body              (comps/|nuds-text {:label (props/|rosetta-plain-string "")})
    :bottom-bar        (comps/|bottom-bar
                        {:primary (comps/|button
                                   {:key        (|of "primary")
                                    :type       (props/|button-type :primary)
                                    :on-pressed (|fn []
                                                     (nuvigator/|open (nuvigator/|deeplink "nuapp://bdc/nu-bdc-sandbox/expr/transition")
                                                                      {:push-method (nuvigator/|push-method :pop-and-push)}))
                                    :child      (comps/|nuds-text {:label (props/|rosetta-plain-string "Sim")})
                                    :expand     true})})}))
