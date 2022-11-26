# localnews-helm

![Version: 1.0.0](https://img.shields.io/badge/Version-1.0.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 1.0.0](https://img.shields.io/badge/AppVersion-1.0.0-informational?style=flat-square)

A Helm chart for the localnews App

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| feedscraper.deployment | string | `"on"` | Whether the feedscraper should be deployed. |
| feedscraper.envVars.backend.key | string | `"SCRAPER_FEED_BACKEND_HOST"` | The key for another environment variable. This is used to set the backend host. Must point to the service name of the backend. |
| feedscraper.envVars.backend.value | string | `"news-backend"` | The value pointing to the service. |
| feedscraper.envVars.feeds.key | string | `"SCRAPER_FEEDS_URL"` | The key for the environment variable to be set. This is used to configure the feeds url. |
| feedscraper.envVars.feeds.value | string | `"http://feeds.bbci.co.uk/news/world/rss.xml, https://www.aljazeera.com/xml/rss/all.xml, https://www.nytimes.com/svc/collections/v1/publish/https://www.nytimes.com/section/world/rss.xml, http://rss.cnn.com/rss/edition_world.rss, https://timesofindia.indiatimes.com/rssfeeds/296589292.cms, https://www.cnbc.com/id/100727362/device/rss/rss.html"` | The value for the environment variable. This is used to set a comma-separated list of urls to be scraped.  |
| feedscraper.image | string | `"quay.io/k8snativedev/feed-scraper"` | Image used for the feedscraper pod. |
| feedscraper.imageTag | string | `"openshift"` | Image tag appended to the image |
| feedscraper.name | string | `"feed-scraper"` | The name of the deployment and service. |
| feedscraper.replicaCount | int | `1` | The number of replicas. Values >1 will lead to duplicates.  |
| localnews.domain | string | `"your-cluster.domain"` | Must be replaced with the apps domain of your ocp cluster, e.g. 'apps.ocp4.example.com' |
| localnews.imagePullPolicy | string | `"Always"` | The general image pull policy for all components |
| localnews.mesh.controlplane.name | string | `"basic"` | The name of the service mesh controlplane CR  |
| localnews.mesh.controlplane.namespace | string | `"istio-system"` | The namespace of the service mesh controlplane |
| localnews.mesh.dataplane.mtls.strict | string | `"off"` | Switch from permissive to strict mode by setting this to 'on'  |
| localnews.mesh.locationExtractorSecondVersion.name | string | `"off"` | The name of a second version of the location extractor component, e.g. 'v2-worse-performance'. Second version will deployed when name != 'off'. |
| localnews.mesh.locationExtractorSecondVersion.type | string | `"50/50"` | Set the routing policy. Supported values are '50/50' and 'mirror'. |
| localnews.servicemesh | string | `"off"` | Enables service mesh for all components except the database |
| localnews.sso.client.secret | string | `"ZHNnc0RHJT9zZGc/c0VUCg=="` | The client secret for the OIDC client |
| localnews.sso.enabled | string | `"off"` | Enables single sign on deployment in another namespace. The instance will be preconfigured with an OIDC client for the localnews app. Requires the RHSSO operator to be deployed in this namespace. |
| localnews.sso.namespacepostfix | string | `"sso"` | The postfix for the namespace where RHSSO will be installed  |
| localnews.workloadmonitoring | string | `"off"` | Enables user workload monitoring by creating a service monitor for news-backend and location-extractor. |
| locationextractor.deployment | string | `"on"` | Enable the deployment of the location extractor. |
| locationextractor.image | string | `"quay.io/k8snativedev/location_extractor"` | The image to be used for the location extractor. |
| locationextractor.imageTag | string | `"openshift"` | The image tag. |
| locationextractor.name | string | `"location-extractor"` |  |
| locationextractor.ports.containerPort | int | `5000` | Container port. |
| locationextractor.ports.servicePort | int | `8081` | Service port. |
| locationextractor.replicaCount | int | `1` | The number of replicas used for the location extractor. |
| locationextractor.service | string | `"on"` | Enable the service for the location extractor. |
| newsbackend.deployment | string | `"on"` | Whether the news backend should be deployed. |
| newsbackend.envVars.backendNlpUrl.key | string | `"backend.nlp.urli"` | The key of the environment variable used to set the url to the location extractor. |
| newsbackend.envVars.backendNlpUrl.valueEndpoint | string | `"/get_loc"` | The path used to analyze the text |
| newsbackend.envVars.backendNlpUrl.valueHost | string | `"http://location-extractor"` | Protocol and host name for the location extractor. This should point to the service name. |
| newsbackend.envVars.jdbcUrl.key | string | `"quarkus.datasource.jdbc.url"` | The key of the environment variable used to set the url to the database. |
| newsbackend.envVars.jdbcUrl.value | string | `"jdbc:postgresql://postgis:5400/news?user=postgres&password=banane"` |  |
| newsbackend.image | string | `"quay.io/k8snativedev/news-backend"` | The image for the backend. |
| newsbackend.imageTag | string | `"openshift"` | The image tag for the backend. |
| newsbackend.name | string | `"news-backend"` | Name of the Kubernetes service. |
| newsbackend.ports.containerPort | int | `8080` | The port of the container  |
| newsbackend.ports.nodePort | int | `30000` | Node port used to access the backend from the browser. This is required when localnews is deployed without ingress. |
| newsbackend.ports.servicePort | int | `8080` | Port of the service |
| newsbackend.replicaCount | int | `1` | The number of replicas. |
| newsbackend.service | string | `"on"` | Whether the Kubernetes service should be deployed. |
| newsfrontend.backendConnection | string | `"viaIngress"` | How to access the backend. 'viaIngress' or 'viaNodePort'. |
| newsfrontend.deployment | string | `"on"` | Enable or disable the deployment of the frontend. |
| newsfrontend.envVars.backend.ip.key | string | `"NODE_IP"` |  |
| newsfrontend.envVars.backend.ip.value | string | `nil` | with 'viaNodePort' no value needs to be set because ip is retrieved from kubernetes api |
| newsfrontend.envVars.backend.nodePort.key | string | `"NODE_PORT"` |  |
| newsfrontend.envVars.backend.nodePort.value | int | `80` | with 'viaNodePort' no value needs to be set because nodePort is specified in the news-backend Service |
| newsfrontend.envVars.backend.prefix.key | string | `"PREFIX"` |  |
| newsfrontend.envVars.backend.prefix.value | string | `"http://"` | The protocol to be used. You can switch between http and https. |
| newsfrontend.image | string | `"quay.io/k8snativedev/news-frontend"` | Image used for the frontend. |
| newsfrontend.imageTag | string | `"openshift"` | Image tag. |
| newsfrontend.name | string | `"news-frontend"` | The name of the service and deployment  |
| newsfrontend.ports.containerPort | int | `8080` |  |
| newsfrontend.ports.nodePort | int | `31111` |  |
| newsfrontend.ports.servicePort | int | `80` |  |
| newsfrontend.replicaCount | int | `1` | Number of replias |
| newsfrontend.service | string | `"on"` | Enable or disable the service. |
| postgis.envVars.PGDATA.key | string | `"PGDATA"` | Where to store the data for postgres. |
| postgis.envVars.PGDATA.value | string | `"/tmp/pgdata"` | The path where the postgres data is stored. Points to tmp since we use ephemeral storage by default. |
| postgis.envVars.POSTGRES_DB.key | string | `"POSTGRES_DB"` | The name of the database. |
| postgis.envVars.POSTGRES_DB.value | string | `"news"` | The name of the database. |
| postgis.envVars.POSTGRES_PASSWORD.key | string | `"POSTGRES_PASSWORD"` | The database password. |
| postgis.envVars.POSTGRES_PASSWORD.value | string | `"banane"` | Set your own password. |
| postgis.envVars.POSTGRES_USER.key | string | `"POSTGRES_USER"` | The database user. |
| postgis.envVars.POSTGRES_USER.value | string | `"postgres"` | The database user. |
| postgis.image | string | `"postgis/postgis:12-master"` | The image used for postgres with the postgis extension. |
| postgis.name | string | `"postgis"` | Name of the postgres deployment |
| postgis.ports.containerPort | int | `5432` | Port of the database container. |
| postgis.ports.servicePort | int | `5400` | Port of the service |
| postgis.replicaCount | int | `1` | Number of replicas. This should not be set >1 because the database cannot be scaled. |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.11.0](https://github.com/norwoodj/helm-docs/releases/v1.11.0)
