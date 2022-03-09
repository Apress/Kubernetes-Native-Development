### The Kubernetes Downward API
## Listing 4-1 News-Backend Kubernetes Deployment with prod/dev Switch
kubectl create namespace news-backend-prod-dev
kubectl -n news-backend-prod-dev apply -f snippets/chapter4/downward
## Listing 4-3. Get News-Backend Pod Logs showing Dev profile
kubectl -n news-backend-prod-dev get pods
kubectl -n news-backend-prod-dev logs deployments/news-backend
## Set dev label to false in downward/news-backend-deployment.yaml then apply
kubectl -n news-backend-prod-dev apply -f snippets/chapter4/downward/news-backend-deployment.yaml
kubectl -n news-backend-prod-dev logs deployments/news-backend
## Cleanup
kubectl delete namespace news-backend-prod-dev
### Interacting with the Kubernetes API
## Access the Kube-apiserver
kubectl proxy --port 8080
## Deploy Local News application with Helm
kubectl create namespace localnews
helm -n localnews install localnews k8s/helm-chart
## check how to retrieve deployments directly via api
curl http://localhost:8080/apis/apps/v1/namespaces/localnews/deployments
curl http://localhost:8080/apis/apps/v1/namespaces/localnews/deployments/news-backend
## Send Unauthorized POST Request to News-Backend
minikube -n localnews service news-backend
curl -X POST -H "Content-Type: application/json" -d '["https://www.nytimes.com/svc/collections/v1/publish/https://www.nytimes.com/section/world/rss.xml"]' $(echo | minikube -n localnews service news-backend --url)/scraper
kubectl -n localnews logs deploy/news-backend
## Authorize News-Backend to Create Deployments
kubectl -n localnews apply -f snippets/chapter4/api
curl -X POST -H "Content-Type: application/json" -d '["https://www.nytimes.com/svc/collections/v1/publish/https://www.nytimes.com/section/world/rss.xml"]' $(echo | minikube -n localnews service news-backend --url)/scraper
### Defining our own Kubernetes resource types
## Change Feed URL
kubectl apply -f snippets/chapter4/scraper/feed-scraper-deployment.yaml
## Create CRD
kubectl create -f snippets/chapter4/crd/feedanalysis-crd.yaml
## Cleanup
helm -n localnews uninstall localnews
kubectl delete namespace localnews

