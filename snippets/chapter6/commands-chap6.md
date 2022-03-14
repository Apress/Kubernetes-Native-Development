# Commands Chapter 6

#### getting ready
    minikube delete
    minikube start --addons=ingress --vm=true --kubernetes-version='v1.22.3' --memory='8g' --cpus='4' --disk-size='25000mb'

## Kubernetes Operators - How to automate Operations

#### check your current context
    kubectl config current-context
#### Initializing the Operator
    cd k8s/operator/localnews-operator-helm
    operator-sdk init --domain apress.com --group kubdev --version v1alpha1 --kind LocalNewsApp --plugins helm --helm-chart ../../helm-chart
#### Run the Operator
    cd ../../..
    make -C k8s/operator/localnews-operator-helm install run
#### Create an instance of the Application in a new Terminal
    kubectl apply -f snippets/chapter6/news-sample1.yaml
    helm list
    kubectl describe LocalNewsApp mynewsapp
    kubectl get pods
### Modifying the CRD by adding Helm parameters
    kubectl apply -f snippets/chapter6/news-sample2.yaml
    helm list

#### Manual update of the file snippets/chapter6/news-sample3.yaml
    minikube ip
#### automated update with BASH commands
    kubectl apply -f snippets/chapter6/news-sample3.yaml
    MINIKUBEIP=$(minikube ip)
    PATCH_JSON="{\"spec\": {\"localnews\": {\"minikubeIp\": \"$MINIKUBEIP\"}}}"
    kubectl patch LocalNewsApp mynewsapp --type merge -p "$PATCH_JSON"
    ## get the URLs
    kubectl describe LocalNewsApp mynewsapp | grep host
    ## or
    kubectl get ingress

### Modifying resources owned by the Operator

#### delete a resource managed by the Operator
    kubectl delete deployment location-extractor
    kubectl get deployments
#### edit(manually) or patch a resource to increase replicas (which the Operator reverts)
    PATCH_JSON2="{\"spec\": {\"replicas\": 2}}"
    kubectl patch deployment location-extractor --type merge -p "$PATCH_JSON2"
    kubectl describe deployments location-extractor
#### something not defined in the Operator at all can be edited
    kubectl label deployments location-extractor untouched=true
    kubectl get deployments location-extractor --show-labels

#### Delete the CR and the CRD
    kubectl delete -f snippets/chapter6/news-sample3.yaml
    helm list
    ## Ctrl+C in the "make" Terminal Session

#### One chart to rule them all? - Adding a FeedAnalysis CRD
    cd k8s/operator/localnews-operator-helm
    operator-sdk create api --group kubdev --version v1alpha1 --kind FeedAnalysis

#### edit the folder Kubernetes-Native-Development/k8s/operator/localnews-operator-helm/helm-charts/feedanalysis/templates according to the books text and edit the Helm Values file at Kubernetes-Native-Development/k8s/operator/localnews-operator-helm/helm-charts/feedanalysis/values.yaml

#### edit the file Kubernetes-Native-Development/k8s/operator/localnews-operator-helm/helm-charts/feedanalysis/templates/feed-scraper-deployment.yaml according to Listing 6-14 and Listing 6-15 from the book

#### apply the changes and restart the Operator install
    make -C k8s/operator/localnews-operator-helm install run
    ## change terminal session to root folder
#### install the Local News App by applying a new CR
    kubectl apply -f snippets/chapter6/news-sample4.yaml
#### create a FeedScraper instance via a CR
    kubectl apply -f snippets/chapter6/feeds-sample1.yaml
    kubectl apply -f snippets/chapter6/feeds-sample2.yaml
    kubectl get pods

#### Cleaning Up
    kubectl delete -f snippets/chapter6/feeds-sample1.yaml
    kubectl delete -f snippets/chapter6/feeds-sample2.yaml
    kubectl delete -f snippets/chapter6/news-sample4.yaml
    ## Ctrl+C in the make Terminal Session

### Deploying our Operator to Kubernetes

#### build an Image of the Operator
    ## e.g. quay.io/k8snativedev/... or docker.io/yourusername/...
    IMG=<your-repo>/news-operator:0.0.1
    ## dont forget to docker login
    make -C k8s/operator/localnews-operator-helm docker-build docker-push IMG=$IMG    
#### install the operator to the cluster
    make -C k8s/operator/localnews-operator-helm deploy IMG=$IMG
    kubectl -n localnews-operator-helm-system get pods

    kubectl apply -f snippets/chapter6/news-sample1.yaml
    kubectl -n localnews-operator-helm-system logs deployments/localnews-operator-helm-controller-manager manager -f

#### Clean Up
    kubectl delete -f snippets/chapter6/news-sample1.yaml
    make -C k8s/operator/localnews-operator-helm undeploy

## Advanced: Go-based Operator with Operator SDK

---- skipped because there are many code snippets, that need explanation, follow the instructions in the book ----

## Advanced: The Operator Lifecycle Manager - Who manages the operators?
    minikube delete
    minikube start --addons=ingress --vm=true --kubernetes-version='v1.22.3' --memory='8g' --cpus='4' --disk-size='25000mb'

### Deploying Operators via OLM without Operator SDK
    operator-sdk olm install --version v0.19.1
#### Generating the OLM bundle for our own Operator
    ## e.g. quay.io/k8snativedev/... or docker.io/yourusername/...
    IMG=<your-repo>/news-operator:0.0.1
    make -C k8s/operator/localnews-operator-helm bundle IMG=$IMG
#### Building and pushing the OLM bundle image
    ## e.g. quay.io/k8snativedev/... or docker.io/yourusername/...
    BUNDLE_IMG=<your-repo>/localnews-operator-bundle:v0.0.1
    make -C k8s/operator/localnews-operator-helm bundle-build BUNDLE_IMG=$BUNDLE_IMG
    make -C k8s/operator/localnews-operator-helm bundle-push BUNDLE_IMG=$BUNDLE_IMG

#### Installing the OLM bundle
    operator-sdk run bundle $BUNDLE_IMG
    kubectl logs deployments/localnews-operator-helm-controller-manager manager -f
#### test whether the Operator sets up the new application
    kubectl apply -f snippets/chapter6/news-sample2.yaml

#### inspect the component via grpc
    kubectl port-forward quay-io-k8snativedev-news-operator-bundle-v0-0-1 50051:50051
    ## new terminal
    grpcurl -plaintext localhost:50051 api.Registry/ListPackages
    grpcurl -plaintext -d '{"name":"localnews-operator-helm"}' localhost:50051   api.Registry/GetPackage
    grpcurl -plaintext -d '{"pkgName":"localnews-operator-helm","channelName":"alpha"}' localhost:50051 api.Registry/GetBundleForChannel

### How does the OLM install our Operator?
    kubectl get catalogsources
    kubectl get subscription
    kubectl get installplans
    kubectl get clusterserviceversion

#### clean up
    kubectl delete LocalNewsApp mynewsapp
    kubectl delete subscription localnews-operator-helm-v0-0-1-sub
    kubectl delete clusterserviceversion localnews-operator-helm.v0.0.1
    kubectl delete customresourcedefinition localnewsapps.kubdev.apress.com

## Deploying Operators via OLM without Operator SDK

##### install the OLM if you havent done it previously
    ## operator-sdk olm install --version v0.19.1
    kubectl -n olm get catalogsources
#### create the CatalogSource and Subscribe to it
    kubectl apply -f snippets/chapter6/olm/localnews-catalogsource.yaml
    kubectl apply -f snippets/chapter6/olm/localnews-subscription.yaml
    ### optional for testing "kubectl apply -f snippets/chapter6/news-sample2.yaml"

## Operators love GitOps 

#### start with a clean cluster
    minikube delete
    minikube start --addons=ingress --vm=true --kubernetes-version='v1.22.3' --memory='8g' --cpus='4' --disk-size='25000mb'

#### install (again) the OLM and the GitOps Tool ArgoCd
    operator-sdk olm install --version v0.19.1
    kubectl create namespace argocd
    kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/v2.1.7/manifests/install.yaml

#### install the Operator (CatalogSource and Subscription) and the Application via a CR
    kubectl -n argocd apply -f snippets/chapter6/gitops/olm-application.yaml
    kubectl -n argocd apply -f snippets/chapter6/gitops/crd-application.yaml

#### access ArgoCd
    ## get password for admin user
    kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
    ## make available via localhost:8080
    kubectl port-forward svc/argocd-server -n argocd 8080:443

#### check via kubectl whether deployment was successfull
    kubectl get pods -n localnews-gitops-operators