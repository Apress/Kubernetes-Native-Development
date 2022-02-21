### The Local News Application
## Install LocalNews via Helm
kubectl apply -f k8s/plain-manifests/
minikube -n default service news-frontend --url
### Excursion: Discussing Non-Functional Requirements for Kubernetes-native applications
## Optimizing Process Startup Time - Create the News-Backend and its dependencies
kubectl create ns localnews
kubectl -n localnews create -f snippets/chapter2/startuptime/postgis-deployment.yaml
kubectl -n localnews create -f snippets/chapter2/startuptime/postgis-service.yaml
kubectl -n localnews create -f snippets/chapter2/startuptime/location-extractor-deployment.yaml
kubectl -n localnews create -f snippets/chapter2/startuptime/location-extractor-service.yaml

kubectl -n localnews create -f snippets/chapter2/startuptime/news-backend-deployment.yaml
kubectl -n localnews describe pods news-backend-<your-id>

kubectl -n localnews logs news-backend-<your-id>
## Optimizing Process Startup Time - Create the native News-Backend
kubectl -n localnews create -f snippets/chapter2/startuptime/news-backend-native-deployment.yaml

kubectl -n localnews logs news-backend-native-<your-id>
## Optimizing Pull Time Building a Distroless Image and Pulling an Application Server Image
git clone https://github.com/GoogleContainerTools/distroless.git
cd distroless/examples/go
docker build --tag distrolessgo:latest .
docker pull jboss/wildfly:23.0.1.Final
docker images






