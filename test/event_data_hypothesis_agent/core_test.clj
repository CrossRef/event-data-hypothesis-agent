(ns event-data-hypothesis-agent.core-test
  (:require [clojure.test :refer :all]
            [event-data-hypothesis-agent.core :as core]))

(def input
{:tags [],
 :permissions
 {:read ["group:__world__"],
  :admin ["acct:haiyahirai@hypothes.is"],
  :update ["acct:haiyahirai@hypothes.is"],
  :delete ["acct:haiyahirai@hypothes.is"]},
 :group "__world__",
 :updated "2017-04-11T17:04:19.398958+00:00",
 :flagged false,
 :created "2017-04-11T17:04:19.398958+00:00",
 :references ["rQfcjv4_EeaOmPNbl5cVgQ"],
 :document
 {:title
  ["Joint Statement of Current Law on Religion in the Public Schools"]},
 :hidden false,
 :id "5vfZnh7YEeelbJvmtQoBvQ",
 :uri
 "https://www.aclu.org/other/joint-statement-current-law-religion-public-schools",
 :target
 [{:source
   "https://www.aclu.org/other/joint-statement-current-law-religion-public-schools"}],
 :user "acct:haiyahirai@hypothes.is",
 :links
 {:json "https://hypothes.is/api/annotations/5vfZnh7YEeelbJvmtQoBvQ",
  :html "https://hypothes.is/a/5vfZnh7YEeelbJvmtQoBvQ",
  :incontext
  "https://hyp.is/rQfcjv4_EeaOmPNbl5cVgQ/www.aclu.org/other/joint-statement-current-law-religion-public-schools"},
 :text
 "I agree, I don't think they are taking the right action even though the law is murky."})

(def expected-actions
  [{:id "hypothesis-5vfZnh7YEeelbJvmtQoBvQ",
    :url "https://hypothes.is/a/5vfZnh7YEeelbJvmtQoBvQ",
    :relation-type-id "annotates",
    :occurred-at "2017-04-11T17:04:19Z",
    :observations
    [{:type :url,
      :input-url "https://www.aclu.org/other/joint-statement-current-law-religion-public-schools"}],
    :extra {},
    :subj
    {:json-url "https://hypothes.is/api/annotations/5vfZnh7YEeelbJvmtQoBvQ",
     :pid "https://hypothes.is/a/5vfZnh7YEeelbJvmtQoBvQ",
     :url "https://hyp.is/rQfcjv4_EeaOmPNbl5cVgQ/www.aclu.org/other/joint-statement-current-law-religion-public-schools",
     :type "annotation",
     :title "I agree, I don't think they are taking the right action even though the law is murky.",
     :issued "2017-04-11T17:04:19Z"
     :alternative-id "5vfZnh7YEeelbJvmtQoBvQ"}}

    {:id "hypothesis-5vfZnh7YEeelbJvmtQoBvQ-text",
     :url "https://hypothes.is/a/5vfZnh7YEeelbJvmtQoBvQ",
     :relation-type-id "discusses",
     :occurred-at "2017-04-11T17:04:19Z",
     :observations
     [{:type :plaintext
       :input-content "I agree, I don't think they are taking the right action even though the law is murky."}],
     :extra {},
     :subj
     {:json-url "https://hypothes.is/api/annotations/5vfZnh7YEeelbJvmtQoBvQ",
      :pid "https://hypothes.is/a/5vfZnh7YEeelbJvmtQoBvQ",
      :url "https://hyp.is/rQfcjv4_EeaOmPNbl5cVgQ/www.aclu.org/other/joint-statement-current-law-religion-public-schools",
      :type "annotation",
      :title "I agree, I don't think they are taking the right action even though the law is murky.",
      :issued "2017-04-11T17:04:19Z"
      :alternative-id "5vfZnh7YEeelbJvmtQoBvQ"}}])

(deftest can-parse-item
  (testing "Input parsed JSON can be parsed into Action with Observations."
    (is (= (core/api-item-to-actions input) expected-actions))))
