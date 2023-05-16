(ns custom-loading
  (:require [archstone.catalyst.components.api :as comps]
            [archstone.catalyst.components.props.api :as props]
            [archstone.catalyst.core :refer :all]
            [archstone.catalyst.globals.flutter.nuvigator :as nuvigator]
            [archstone.schemata.context.authenticated :as schemata.context]
            [archstone.catalyst.components.bdc.riverpod-bloc-builder.helpers :as riverpod-bloc-helpers]))

(def loading-screen
  (comps/|column
   {:children [(comps/|nuds-text {:label (props/|rosetta-plain-string "Carregando")})
               (comps/|loading-indicator {})
               (comps/|nuds-text {:label (props/|rosetta-plain-string "Calma q vai dar certo")})]}))

(def success-screen
  (comps/|column
   {:children [(comps/|nuds-text {:label (props/|rosetta-plain-string "Tudo certo por aqui!")})
               (comps/|bottom-bar
                {:primary (comps/|button
                           {:child      (comps/|text {:label "Fechar"})
                            :on-pressed (|fn []
                                             (nuvigator/|pop))})})]}))

(defn screen [_ _]
  (comps/|riverpod-bloc-builder
   {:initial-state    riverpod-bloc-helpers/|initial-loading-state
    :initial-dispatch (|fn [dispatch] (riverpod-bloc-helpers/|event dispatch :request))
    :bloc             {:request (riverpod-bloc-helpers/|request-state {:url "http://localhost:8080/mock/severino/api/challenge/1/confirm/success"})}
    :builder          (riverpod-bloc-helpers/|request-state-builder
                       {:loading (|fn [] loading-screen)
                        :success (|fn [state dispatch] success-screen)
                        :error   (|fn [error dispatch] (comps/|text {:label "Loaded error"}))})}))
