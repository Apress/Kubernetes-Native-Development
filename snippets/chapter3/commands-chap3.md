# Commands Chapter 3

   ##### if you haven't done so, clone the Git repo
    git clone https://github.com/Apress/Kubernetes-Native-Development

   ##### start with a clean instance
    minikube delete
    minikube start --addons=ingress --vm=true --kubernetes-version='v1.22.3' --memory='8g' --cpus='4' --disk-size='25000mb'

### 1: Docker-Less

##### Listing 3-1. Run the News-Frontend “Docker-Less”
    cd components/news-frontend
    npm install -g @angular/cli@12.0.3
    npm install
    ng serve

### 2: Dev-to-Docker

##### Building the Container Image for the News-Frontend from components/news-frontend folder (Listing 3-2)
    docker build -f Dockerfile.dev -t frontend:dev .

##### Run the container image (make sure your local npm process is closed, otherwise you'll have port issues)
    docker run -p 4200:4200 frontend:dev

##### Access it at
  [http://localhost:4200](http://localhost:4200)

##### Stop the running container (CTRL-C or "docker ps" and "docker rm -f IDOFTHECONTAINER) and start mounting src code
    docker run -p 4200:4200 -v $PWD/src:/opt/app-root/src/src frontend:dev

##### start the complete application, local with docker-compose (Listing 3-3)
    docker-compose -f components/docker-compose.dev-build.yaml up

##### stop it with CTRL+C and start an "frontend-dev-only" docker-compose (Listing 3-4)
    docker-compose -f components/news-frontend/docker-compose.yaml up

### 3: Dev-to-K8s

##### Listing 3-6. Manual build, push, and deploy process
    cd components/feed-scraper
    docker build -f Dockerfile.build-multi -t feed-scraper .
    docker tag feed-scraper quay.io/k8snativedev/feed-scraper
    docker push quay.io/k8snativedev/feed-scraper

##### Listing 3-8. Imperative vs. declarative 
    #imperative
    kubectl create deployment feed-scraper --image=quay.io/k8snativedev/feed-scraper --replicas=1
    #declarative
    kubectl apply -f k8s/plain-manifests/feed-scraper-deployment.yaml


## Clean Up
    kubectl delete -f k8s/plain-manifests/feed-scraper-deployment.yaml

#### Dev-toK8s: Local development - Execution on Kubernetes

##### Listing 3-10. Deploying the News-Backend with odo on Kubernetes
    cd components/news-backend
    kubectl create namespace news-backend-dev
    odo push --project news-backend-dev
    odo watch --project news-backend-dev

##### Change something e.g. the file components/news-backend/src/main/resources/META-INF/resources/index.html
##### Open another terminal to port-forward the news-backend deployment to your machine
    kubectl -n news-backend-dev port-forward deployments/news-backend-news-backend 8080:8080

##### check whether making changes works via hot-reload in the browser
  [http://localhost:8080](http://localhost:8080)

##### the Logs also show that the news-backend wants a Database to be present
    odo log news-backend --project news-backend-dev

##### Listing 3-12. Helm Chart Installation
    helm install news-backend-dev k8s/helm-chart -n news-backend-dev --set newsbackend.deployment="off"

##### check status of the Pods
    kubectl get pods -n news-backend-dev

##### after successful installation the News-Frontend should be available
    minikube service news-frontend -n news-backend-dev
    #### alternatively in a cloud cluster 
    kubectl get nodes -o wide
    #### IP-of-a-Node:31111

##### make changes to test REST endpoints such as /news of the news-backend directly and check them e.g. via Browser or Postman
    minikube service news-backend -n news-backend-dev”

##### Listing 3-14: Clean Up
    odo delete news-backend --project news-backend-dev
    helm uninstall news-backend-dev -n news-backend-dev
    kubectl delete namespace news-backend-dev


#### Dev-toK8s: Local development - Hybrid Execution

##### Listing 3-15. Helm Chart Installation
    kubectl create namespace location-extractor-dev
    helm install location-extractor-dev k8s/helm-chart -n location-extractor-dev
    kubectl get pods -n location-extractor-dev
    minikube service news-frontend -n location-extractor-dev

##### Listing 3-16. Intercepting Traffic to Location-Extractor component
    telepresence connect
    telepresence list -n location-extractor-dev
    cd components/location_extractor
    telepresence intercept --port 5000:8081 --namespace location-extractor-dev --env-file location-extractor.env location-extractor
    docker build -f Dockerfile.dev -t location-extractor:dev .
    docker run --name location-extractor -p 5000:5000 -v $(pwd)/src:/app/src/ --env-file location-extractor.env location-extractor:dev

##### Listing 3-17. Testing the Hybrid Connection
    kubectl create deployment nginx --image=nginx --namespace=location-extractor-dev
    kubectl exec -it -n location-extractor-dev nginx-YOURPODID -- curl location-extractor:8081/get_loc\?text=Frankfurt 

##### checking whether request pop up locally
    docker logs location-extractor

##### create the Feedscraper to add additional news items from other sources (here CNN)
    kubectl create -n location-extractor-dev -f k8s/plain-manifests/feed-scraper-job.yaml    

##### again check incoming requests to locally running service
    docker logs location-extractor

##### Check for additional news items on the frontend
    minikube service news-frontend -n location-extractor-dev

##### Listing 3-19. Integration Testing in the Dev-Stage
    telepresence leave location-extractor-location-extractor-dev
    helm uninstall location-extractor-dev -n location-extractor-dev
    kubectl delete namespace location-extractor-dev

#### Dev-toK8s: Development on Kubernetes - Execution on Kubernetes

##### shortcut to get Eclipse che Secret 
##### Listing 3-20. Retrieve a Value from a Kubernetes Secret and decode it
    kubectl get secret self-signed-certificate -n eclipse-che -o json | jq -r '.data."ca.crt"' | base64 --decode > checa.crt

##### Get the Che installation URL
    kubectl get ingress -n eclipse-che

##### Listing 3-21. Creating a Development Workspace in Che
    chectl auth:login
    chectl workspace:create --devfile=snippets/chapter3/che-devfiles/localnews-devfile-all-dev.yaml

##### stop the workspace
    chectl workspace:list
    chectl workspace:stop <workspace-id>

##### Listing 3-23. Deploying all components except News-Frontend with Helm
    chectl auth:login
    chectl workspace:create --devfile=snippets/chapter3/che-devfiles/localnews-devfile-frontend-only.yaml
    helm install news-frontend-dev k8s/helm-chart -n admin-che --set newsfrontend.deployment="off" --set newsfrontend.service="off" --set newsfrontend.backendConnection="viaIngress" --set localnews.minikubeIp=$(minikube ip) --set newsfrontend.ports.servicePort=4200

##### retrieve address of news-backend
    kubectl get ingress -n admin-che

##### replace it in components/news-frontend/src/assets/settings.json with the Ingress URL of the News-Backend

##### for minikube get the IP with
    minikube ip

##### Listing 3-25. Cleaning Up
    chectl auth:login
    chectl workspace:list
    chectl workspace:stop <id-of-your-workspace>
    helm uninstall news-frontend-dev -n admin-che















