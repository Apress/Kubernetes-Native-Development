## get ready
minikube delete
minikube start --addons=ingress --vm=true --kubernetes-version='v1.22.3' --memory='8g' --cpus='4' --disk-size='25000mb'
## initialize the operator
cd k8s/operator/localnews-operator
operator-sdk init --domain apress.com --group kubdev --version v1alpha1 --kind LocalNewsApp --plugins helm --helm-chart ../../helm-chart
## leave open
make install run
## new terminal
kubectl apply -f snippets/chapter6/news-sample1.yaml
helm list
kubectl get pods
kubectl get localnewsapp mynewsapp -o yaml

kubectl apply -f snippets/chapter6/news-sample2.yaml
helm-list

## manual update of file snippets/chapter6/news-sample2a.yaml
minikube ip
## automated update with BASH commands
kubectl apply -f snippets/chapter6/news-sample2a.yaml
MINIKUBEIP=$(minikube ip)
PATCH_JSON="{\"spec\": {\"localnews\": {\"minikubeIp\": \"$MINIKUBEIP\"}}}"
kubectl patch LocalNewsApp mynewsapp --type merge -p "$PATCH_JSON"

## OLM
operator-sdk olm install
make docker-build docker-push IMG="docker.io/maxisses/localnews-operator:0.0.1"
## run and fill in prompts
make bundle IMG="docker.io/maxisses/localnews-operator:0.0.1"


make bundle-build BUNDLE_IMG="docker.io/maxisses/localnews-operator-bundle:v0.0.1"
make bundle-push BUNDLE_IMG="docker.io/maxisses/localnews-operator-bundle:v0.0.1"
operator-sdk run bundle docker.io/maxisses/localnews-operator-bundle:v0.0.1

kubectl get pods
