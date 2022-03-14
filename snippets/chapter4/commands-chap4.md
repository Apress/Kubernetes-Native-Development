# Commands Chapter 4
   ##### if you haven't done so, clone the Git repo
    git clone https://github.com/Apress/Kubernetes-Native-Development

   ##### start with a clean instance
    minikube delete
    minikube start --addons=ingress --vm=true --kubernetes-version='v1.22.3' --memory='8g' --cpus='4' --disk-size='25000mb'
## The Kubernetes Downward API
#### Listing 4-1 News-Backend Kubernetes Deployment with prod/dev Switch
    kubectl create namespace news-backend-prod-dev
    kubectl -n news-backend-prod-dev apply -f snippets/chapter4/downward
#### Listing 4-3. Get News-Backend Pod Logs showing Dev profile
    kubectl -n news-backend-prod-dev get pods
    kubectl -n news-backend-prod-dev logs deployments/news-backend
#### Set dev label to false in downward/news-backend-deployment.yaml then apply
    kubectl -n news-backend-prod-dev apply -f snippets/chapter4/downward/news-backend-deployment.yaml
    kubectl -n news-backend-prod-dev logs deployments/news-backend
#### Cleanup
    kubectl delete namespace news-backend-prod-dev
## Interacting with the Kubernetes API
#### Access the Kube-apiserver
    kubectl proxy --port 8080
#### Deploy Local News application with Helm
    kubectl create namespace localnews
    helm -n localnews install localnews k8s/helm-chart
#### check how to retrieve deployments directly via api
    curl http://localhost:8080/apis/apps/v1/namespaces/localnews/deployments
    curl http://localhost:8080/apis/apps/v1/namespaces/localnews/deployments/news-backend
#### Send an unauthorized POST Request to News-Backend
    minikube -n localnews service news-backend
    curl -X POST -H "Content-Type: application/json" -d '["https://www.nytimes.com/svc/collections/v1/publish/https://www.nytimes.com/section/world/rss.xml"]' $(echo | minikube -n localnews service news-backend --url)/scraper
    kubectl -n localnews logs deploy/news-backend
#### Authorize News-Backend to Create Deployments
    kubectl -n localnews apply -f snippets/chapter4/api
    curl -X POST -H "Content-Type: application/json" -d '["https://www.nytimes.com/svc/collections/v1/publish/https://www.nytimes.com/section/world/rss.xml", "http://rss.cnn.com/rss/edition_world.rss"]' $(echo | minikube -n localnews service news-backend --url)/scraper
    kubectl -n localnews get deployments
    minikube service news-frontend -n localnews
#### Clean Up
    helm -n localnews uninstall localnews
    kubectl delete namespace localnews
## Defining our own Kubernetes resource types
#### Create CRD from Listing 4-14. Custom Resource Definition Manifest in YAML
    kubectl -n localnews create -f snippets/chapter4/crd/feedanalysis-crd.yaml
    kubectl get feedanalyses
    kubectl get fa
#### Create an instance of the CRD by creating a CR
    kubectl create ns localnews
    kubectl -n localnews create -f snippets/chapter4/crd/my-feed-analysis.yaml

#### Clean Up
    kubectl delete ns localnews

### Attempt 1 - Let the backend do the job

#### Listing 4-18. Deploying the new News-Backend Logic 
    kubectl apply -f snippets/chapter4/crd/feedanalysis-crd.yaml
    kubectl create namespace attempt1
    kubectl -n attempt1 apply -f snippets/chapter4/attempt1
    kubectl -n attempt1 apply -f snippets/chapter4/crd/my-feed-analysis.yaml

## Attempt 2 - Extending the Feed-Scraper

#### Clean Up
    kubectl delete namespace attempt1

#### Listing 4-21. Deploying the new Feed-Scraper Route 
    kubectl apply -f snippets/chapter4/crd/feedanalysis-crd.yaml
    kubectl create namespace attempt2
    kubectl -n attempt2 apply -f snippets/chapter4/attempt2
    kubectl -n attempt2 create -f snippets/chapter4/crd/my-feed-analysis.yaml
    kubectl -n attempt2 logs deployment/feed-scraper -f

## Attempt 3 - Writing a custom controller

#### Clean Up
    kubectl delete namespace attempt2

#### Listing 4-25. Deploying the new Custom Controller
    kubectl apply -f snippets/chapter4/crd/feedanalysis-crd.yaml
    kubectl create namespace attempt3
    kubectl -n attempt3 apply -f snippets/chapter4/attempt3
    kubectl -n attempt3 create -f snippets/chapter4/crd/my-feed-analysis.yaml
    kubectl -n attempt3 logs deployment/feed-scraper -f

## Attempt 4 - Let the Kubernetes Job do the job

#### Clean Up
    kubectl delete namespace attempt3

#### Listing 4-28. Deploying the new Controller that Creates a Job
    kubectl apply -f snippets/chapter4/crd/feedanalysis-crd.yaml
    kubectl create namespace attempt4
    kubectl -n attempt4 apply -f snippets/chapter4/attempt4
    kubectl -n attempt4 create -f snippets/chapter4/crd/my-feed-analysis.yaml
