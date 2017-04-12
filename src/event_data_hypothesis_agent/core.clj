(ns event-data-hypothesis-agent.core
  (:require [org.crossref.event-data-agent-framework.core :as c]
            [event-data-common.status :as status]
            [crossref.util.doi :as cr-doi]
            [clojure.tools.logging :as log]
            [clojure.java.io :refer [reader]]
            [clojure.data.json :as json]
            [clj-time.coerce :as coerce]
            [clj-time.core :as clj-time]
            [throttler.core :refer [throttle-fn]]
            [clj-http.client :as client]
            [config.core :refer [env]]
            [clojure.core.async :refer [>!!]]
            [robert.bruce :refer [try-try-again]]
            [clj-time.format :as clj-time-format])
  (:import [java.util UUID]
           [org.apache.commons.codec.digest DigestUtils])
  (:gen-class))

(def source-token "8075957f-e0da-405f-9eee-7f35519d7c4c")
(def user-agent "CrossrefEventDataBot (eventdata@crossref.org)")
(def license "https://creativecommons.org/publicdomain/zero/1.0/")
(def version (System/getProperty "event-data-hypothesis-agent.version"))
(def api-url "https://hypothes.is/api/search")

; Max according to API docs.
(def page-size 200)

(def date-format
  (:date-time-no-ms clj-time-format/formatters))

(defn api-item-to-action
  [item]
  (let [occurred-at-iso8601 (clj-time-format/unparse date-format (coerce/from-string (:updated item)))]
    {:id (str "hypothesis-" (:id item))
     :url (-> item :links :html)
     :relation-type-id "annotates"
     :occurred-at occurred-at-iso8601
     :observations [{:type :url :input-url (-> item :uri)}]
     :extra {}
     :subj {
      :json-url (-> item :links :json)
      :pid (-> item :links :html)
      :url (-> item :links :incontext)
      :type "annotation"
      :title (-> item :text)
      :issued occurred-at-iso8601}}))

; API
(defn parse-page
  "Parse response JSON to a page of Actions."
  [url json-data]
  (let [parsed (json/read-str json-data :key-fn keyword)]
    {:url url
     :actions (map api-item-to-action (-> parsed :rows))}))

(defn fetch-page
  [offset]
  (status/add! "hypothesis-agent" "hypothesis" "fetch-page" 1)  
  ; If the API returns an error
  (try
    (try-try-again
      {:sleep 30000 :tries 10}
      #(let [result (client/get api-url {:query-params {:offset offset :limit page-size :sort "updated" :order "desc"} :headers {"User-Agent" user-agent}})]
        (log/info "Fetched page" {:offset offset :limit page-size})

        (condp = (:status result)
          200 (parse-page api-url (:body result))
          404 {:url api-url :actions [] :extra {:after nil :before nil :error "Result not found"}}
          
          (do
            (log/error "Unexpected status code" (:status result) "from" api-url)
            (log/error "Body of error response:" (:body result))
            (throw (new Exception "Unexpected status"))))))

    (catch Exception ex (do
      (log/error "Error fetching" api-url)
      (log/error "Exception:" ex)
      {:url api-url :actions [] :extra {:error "Failed to retrieve page"}}))))

(def fetch-page-throttled (throttle-fn fetch-page 20 :minute))

(defn fetch-pages
  "Lazy sequence of pages."
  ([] (fetch-pages 0))
  ([offset]
    (log/info "fetch page offset" offset)
    (let [result (fetch-page-throttled offset)
          actions (:actions result)]

      (log/info "Got" (count actions) "actions")

      (if (empty? actions)
        [result]
        (lazy-seq (cons result (fetch-pages (+ offset page-size))))))))

(defn all-action-dates-after?
  [date page]
  (let [dates (map #(-> % :occurred-at coerce/from-string) (:actions page))]
    (every? #(clj-time/after? % date) dates)))

(defn fetch-parsed-pages-after
  "Fetch seq parsed pages of Actions until all actions on the page happened before the given time."
  [date]
  (let [pages (fetch-pages)]
    (take-while (partial all-action-dates-after? date) pages)))

(defn retrieve-all
  [artifacts bundle-chan]
  (log/info "Start crawl all hypothesis at" (str (clj-time/now)))
  (status/add! "hypothesis-agent" "process" "scan" 1)
  (let [counter (atom 0)
        cutoff-date (-> 12 clj-time/hours clj-time/ago)]
    (let [pages (fetch-parsed-pages-after cutoff-date)]
      (doseq [page pages]
          (let [package {:source-token source-token
                   :source-id "hypothesis"
                   :license license
                   :agent {:version version}
                   :extra {:cutoff-date (str cutoff-date)}
                   :pages [page]}]
        (log/info "Sending package...")
        (>!! bundle-chan package)))))
  (log/info "Finished scan."))

(def agent-definition
  {:agent-name "hypothesis-agent"
   :version version
   :schedule [{:name "retrieve-all"
              :seconds 14400 ; wait four hours between scans
              :fixed-delay true
              :fun retrieve-all
              :required-artifacts []}]
   :runners []})

(defn -main [& args]
  (c/run agent-definition))
