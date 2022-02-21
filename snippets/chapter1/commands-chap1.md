# Commands Chapter 1

## Kubernetes Basics

### Getting started with Kubernetes

##### Listing 1-1. Setting up Minikube on Linux - adjust to your OS based on the official guide
    curl -LO https://storage.googleapis.com/minikube/releases/v1.24.0/minikube-linux-amd64
    sudo install minikube-linux-amd64 /usr/local/bin/minikube
    minikube start --addons=ingress --vm=true --kubernetes-version='v1.22.3' --memory='8g' --cpus='4' --disk-size='25000mb'

### The Kubernetes Architecture

##### curl the Kubernetes API
    kubectl proxy --port=8080
    curl -X GET localhost:8080

##### inspect the nodes endpoint
    curl -X GET localhost:8080/api/v1/nodes

##### check the Kubernetes Components running as Pods
    kubectl get pods -n kube-system

##### check how Kube-Proxy runs as a DaemonSet
    kubectl get daemonsets -n kube-system

### Basic Concepts in Kubernetes

##### Clone the books accompanying Git repository
    git clone https://github.com/Apress/Kubernetes-Native-Development

##### Create your first Pod (always from the root dir of the repo)
    kubectl create -f snippets/chapter1/webserver-pod.yaml

##### Check whether its running
    kubectl get pod webserver

##### extend the view
    kubectl get pod webserver -o yaml

##### Create a ReplicaSet
    kubectl create -f snippets/chapter1/webserver-rs.yaml

##### inspect the Pods spawned by ReplicaSet and the ReplicaSet itself
    kubectl get pods
    kubectl get rs webserver

##### delete a Pod and watch it getting respawned
    kubectl delete webserver-YOURPODID
    kubectl get rs webserver

##### delete the ReplicaSet which also deletes the associated Pods
    kubectl delete -f snippets/chapter1/webserver-rs.yaml

##### create a Deployment and inspect it
    kubectl create -f snippets/chapter1/webserver-deployment.yaml
    kubectl get deployment webserver

##### Inspect the ReplicaSet that the Deployment created
    kubectl get deployment webserver

##### Perform a rolling update by updating your deployment, which results in 2 ReplicaSets
    kubectl apply -f snippets/chapter1/webserver-deployment-1.21.0.yaml

##### Roll back to the previous version
    kubectl rollout undo deployment webserver

##### attach "-o wide" to the pods commands to get its IP address
    kubectl get pods -o wide

##### Take one of the Pod IPs and put it into snippets/chapter1/curl-job-podip.yaml to test whether you can curl it
    kubectl create -f snippets/chapter1/curl-job-podip.yaml

##### Check the Logs to test whether curl was successful
    kubectl get pods -o wide
    kubectl logs curl-YOURPODID

##### Create a Kubernetes Service for the Deployment and inspect it
    kubectl apply -f snippets/chapter1/webserver-svc.yaml
    kubectl get service webserver

##### Curl the Service now, not the Pod directly
    kubectl create -f snippets/chapter1/curl-job-service.yaml

##### Check the Logs of the Pod running the curl command to test whether it reached the webserver
    kubectl get pods
    kubectl logs curl-service--1-YOURPODID

##### Create a NodePort Service
    kubectl apply -f snippets/chapter1/webserver-svc-nodeport.yaml

##### display the webpage on Minikube
    minikube service webserver

##### alternatively fetch IP address of one worker and check the service for the NodePort port and put both in your browser
    kubectl get nodes -o wide
    kubectl get svc

##### Fetch the IP of your Minikube VM
    minikube ip

##### Put it into the file snippets/chapter1/webserver-ingress.yaml and create an Ingress
    kubectl apply -f snippets/chapter1/webserver-ingress.yaml

##### Create a PersistentVolume
    kubectl create -f snippets/chapter1/webserver-pv.yaml
    kubectl get pv

##### Create a PersistentVolumeClaim and inspect it
    kubectl create -f snippets/chapter1/webserver-pvc.yaml
    kubectl get pvc

##### Create a Deployment that creates Pods that bind to the PersistentVolumeClaim
    kubectl apply -f snippets/chapter1/webserver-pvc-deployment.yaml”

##### checking your frontend results in an error...
    minikube service webserver

##### copy a new index.html into your Volume
    kubectl get pods
    kubectl cp snippets/chapter1/index.html webserver-YOURPODID:/usr/share/nginx/html

##### create a ConfigMap
    kubectl apply -f snippets/chapter1/webserver-configmap.yaml

##### Modify the Deployment to use the ConfigMap instead of the PersistentVolume
    kubectl apply -f snippets/chapter1/webserver-configmap-deployment.yaml

##### Create a new Namespace
    kubectl create ns dev
    kubectl get ns

##### Check network connectivity across Namespaces via FQDN and start a Pod in the new Namespace to curl the webserver
    kubectl apply -f snippets/chapter1/curl-job-service-fqdn.yaml -n dev
    kubectl get pods -n dev

##### check the logs to inspect whether curl was successful
    kubectl logs -n dev curl-service-fqdn-YOURPODIP





    



