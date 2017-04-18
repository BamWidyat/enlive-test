(ns enlive-test.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [io.pedestal.interceptor :refer [interceptor]]
            [net.cgrand.enlive-html :as html]
            [io.pedestal.http.route.definition.table :as table]))

(html/deftemplate post-page "test.html"
  [post]
  [:title] (html/content (:title post))
  [:h1] (html/content (:title post))
  [:span.author] (html/content (:author post))
  [:div.post-body] (html/content (:body post)))

(html/deftemplate post-page-2 "test2.html"
  [post]
  [:title] (html/content (:title post))
  [:h1] (html/content (:title post))
  [:p] (html/content (:author post)))

(html/deftemplate post-page-3 "sidebar-test/index.html"
  [post]
  [:title] (html/content (:title post)))

(defn make-html
  [template post]
  (reduce str (template post)))

(def sample-post {:author "Luke VanderHart"
                  :title "Why Clojure Rocks"
                  :body "Functional programming!"})

#_(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(def home-page
  (interceptor
   {:name ::home-page
    :enter (fn [context]
             (assoc context
               :response (ring-resp/response (make-html post-page-3 sample-post))))}))

(def middlewares
  [(body-params/body-params) http/html-body])



(def routes #{["/" :get [home-page] :route-name :home-page]
              #_["/about" :get [about-page] :route-name :about-page]})

(defn make-routes
  [routes]
  (->> routes
       (mapv (fn [r]
               (update r 2 #(into middlewares %))))))

;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by enlive-test.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes (table/table-routes {} (make-routes routes))

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::http/type :jetty
              ;;::http/host "localhost"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})

