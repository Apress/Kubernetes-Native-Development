# Commands Chapter 5

   ##### if you haven't done so, clone the git repo
    git clone https://github.com/Apress/Kubernetes-Native-Development

   ##### start with a clean instance
    minikube delete
    minikube start --addons=ingress --vm=true --kubernetes-version='v1.22.3' --memory='8g' --cpus='4' --disk-size='25000mb'

## How do we install Tekton?

   ##### install Tekton
    kubectl apply --filename https://storage.googleapis.com/tekton-releases/pipeline/previous/v0.30.0/release.yaml
   ##### check the status
    kubectl get pods --namespace tekton-pipelines
   ##### install the Dashboard
    kubectl apply --filename https://storage.googleapis.com/tekton-releases/dashboard/previous/v0.22.0/tekton-dashboard-release.yaml

## Building a Pipeline for the News-Backend

### What is a Tekton Task?
   ##### Create sample git clone Task
    kubectl create ns test-git-clone-task
    kubectl apply -n test-git-clone-task -f snippets/chapter5/custom_tasks/sample-git-tasks/git-clone-simple.yaml
   ##### Check on the Task and Pods
    kubectl -n test-git-clone-task get tasks
    kubectl -n test-git-clone-task get pods
   ##### Start the Task with TaskRun
    kubectl apply -n test-git-clone-task -f snippets/chapter5/custom_tasks/sample-git-tasks/git-clone-taskrun.yaml
   ##### Retrieve the logs from the TaskRun
    kubectl logs -n test-git-clone-task git-clone-task-run-pod step-clone-repo
    kubectl logs -n test-git-clone-task git-clone-task-run-pod step-git-version
   ##### Start a Task with tkn CLI Tool
    tkn -n test-git-clone-task task start git-clone-simple --showlog
   ##### Create the enhanced git clone Task with a parameter for the Git repo
    kubectl apply -n test-git-clone-task -f snippets/chapter5/custom_tasks/sample-git-tasks/git-clone-simple-param.yaml
    tkn -n test-git-clone-task task start git-clone-simple-param --showlog
   ##### Listing 5-8. Create Git Clone and Maven Tasks from Tekton Catalog
    kubectl create namespace localnews-pipelines
    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/maven/0.2/maven.yaml
    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/git-clone/0.5/git-clone.yaml
   ##### Creating a workspace for sharing files between Tasks
    kubectl apply -n localnews-pipelines -f snippets/chapter5/persistence/workspace-simple-pvc.yaml
   ##### Start the new Task with a workspace
    tkn -n localnews-pipelines task start git-clone --showlog
### Assembling the Pipeline
   ###### If you didn't do it so far, now is a good time to make a your own fork of the books Git Repository and use it for all subsequent tasks
##### Listing 5-10. Create Buildah and Helm Tasks from Tekton Catalog
    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/buildah/0.2/buildah.yaml
    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/helm-upgrade-from-source/0.3/helm-upgrade-from-source.yaml

##### Create the Pipeline

    kubectl apply -n localnews-pipelines -f snippets/chapter5/pipelines/java-backend-simple-pipeline.yaml

##### Create a Secret to access your own Container Registry

    ## for security reasons log in with a token, on hub.docker.com you can generate it here: https://hub.docker.com/settings/security
    kubectl create secret docker-registry k8snativedev-tekton-pull-secret -n localnews-pipelines --docker-server=<your-registry-eg-docker.io> --docker-username=<your-name> --docker-password=<your-password>

##### Patch the Secret so Tekton recognizes it

    ## replace "docker.io" with your own Container Registry
    kubectl patch secret k8snativedev-tekton-pull-secret -n localnews-pipelines -p '{"metadata":{"annotations":{"tekton.dev/docker-0": "https://docker.io"}}}'
   ###### Now, according to Listing 5-16, create an SSH Key and add the public key to your Github or any other SCM tool and the private Key to the secret at snippets/chapter5/rbac/github-secret.yaml 
   
##### create ClusterRole, ClusterRoleBinding, ServiceAccount and the Secret with the SSH key for the Git repo
    kubectl apply -n localnews-pipelines -f snippets/chapter5/rbac

##### Listing 5-18. Running the Pipeline with tkn

    ## Create your "Integration Environment" for news-backend
    kubectl create namespace newsbackend-integration
    
    ## Start your Pipeline, thereby creating a PipelineRun
    tkn pipeline start -n localnews-pipelines news-backend-simple-pipe \
    --workspace name=shared-workspace,volumeClaimTemplateFile=snippets/chapter5/persistence/volumeclaimtemplate.yaml \
    --workspace name=maven-settings,emptyDir="" \
    --serviceaccount=clone-and-build-bot \
    --param gitrepositoryurl=<replace> \
    --param image_repo=<replace> \
    --param component=news-backend \
    --param image_version=64a9a8ba928b4e5668ad6236ca0979e0e386f15c \
    --param target_namespace=newsbackend-integration \
    --use-param-defaults

##### check out the progress of the PipelineRun via tkn

    tkn pipelinerun logs news-backend-simple-pipe-run-<your-id> -f -n localnews-pipelines

##### check out the progress of the PipelineRun via Tekton Dashboard

    kubectl port-forward service/tekton-dashboard 9097:9097 -n tekton-pipelines
    ## now head over to your browser at:
   [http://localhost:9097/#/namespaces/localnews-pipelines/pipelineruns](http://localhost:9097/#/namespaces/localnews-pipelines/pipelineruns)

### Triggering the Pipeline with a Git Push 

##### Listing 5-19. Install Tekton Triggers

    kubectl apply -f https://storage.googleapis.com/tekton-releases/triggers/previous/v0.17.1/release.yaml
    kubectl apply --filename https://storage.googleapis.com/tekton-releases/triggers/previous/v0.17.1/interceptors.yaml

##### Listing 5-20. Create EventListener and associated resources
    ## wait until the tekton triggers Pods are up and running and check with
    kubectl get pods --namespace tekton-pipelines
    ## before applying your ownd resources
    kubectl apply -f snippets/chapter5/github_push_listener -n localnews-pipelines

##### Listing 5-21. Trigger the Pipeline with a simulated POST request

    kubectl port-forward -n localnews-pipelines service/el-github-new-push-listener 9998:8080
    
    curl -v \
    -H 'X-GitHub-Event: push' \
    -H 'X-Hub-Signature: sha1=<**!!!recompute  with  the token 'would_come_from_your_github' which is also at  snippets/chapter5/github_push_listener/secret.yaml!!!**>' \
    -H 'Content-Type: application/json' \
    -d '{"ref": "refs/heads/main", "head_commit":{"id": "a99d19668565f80e0a911b9cb22ec5ef48a7e4e8"}, "repository":{"clone_url":"**!!!replace!!!**"}, "image_repo": "**!!!replace!!!**"}' \
    http://localhost:9998

##### Listing 5-22. Generating an HMAC signature (UNIX command), go to  https://www.freeformatter.com/hmac-generator.html for UI based Generator
    echo -n '{"ref": "refs/heads/main", "head_commit":{"id": "a99d19668565f80e0a911b9cb22ec5ef48a7e4e8"}, "repository":{"clone_url":"**!!!replace!!!**"}, "image_repo": "**!!!replace!!!**"}' | openssl sha1 -hmac "would_come_from_your_github"

##### check progress
head over to:
http://localhost:9097/#/namespaces/localnews-pipelines/pipelineruns/

##### upon success check the application

    kubectl describe deployments -n newsbackend-integration news-backend
    minikube service news-frontend -n newsbackend-integration --url

##### clean up the integration environment for the next chapter to work

    kubectl delete namespace newsbackend-integration

## GitOps
### Adapting the Pipeline for GitOps

##### Listing 5-24. Deploy two additional Tasks to adapt the Pipeline for GitOps

    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/yq/0.2/yq.yaml
    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/git-cli/0.3/git-cli.yaml

##### deploy the new Pipeline

    kubectl apply -n localnews-pipelines -f snippets/chapter5/pipelines/java-backend-simple-pipeline-gitops.yaml

### Set up the GitOps Tool ArgoCD

##### Listing 5-26. Install ArgoCD

    kubectl create namespace argocd
    kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/v2.1.7/manifests/install.yaml

##### check whether Pods of ArgoCD are available

    kubectl get pods -n argocd

##### expose ArgoCD to your local machine (and open a new terminal session)
    ## ensure port 8080 is not occupied by any other process running on your machine
    kubectl port-forward svc/argocd-server -n argocd 8080:443

##### Listing 5-27. Retrieve password (from credentials Secret) and access ArgoCD

    kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

##### head over to the UI and log in with the password and user "admin"
[localhost:8080](localhost:8080)
##### Listing 5-29. Commands to set up ArgoCD Application
    ## only required for private repos, update the respective file with your own private SSH Key for e.g. Github
    kubectl apply -n argocd -f snippets/chapter5/gitops/argocd-repoaccess.yaml
    
    ## adjust to your Git repository
    kubectl apply -n argocd -f snippets/chapter5/gitops/argocd-application.yaml

##### check the status of the application in Kubernetes

    kubectl describe application -n argocd localnews

##### check the status of the application in ArgoCD dashboard, once application is deployed and in sync
https://localhost:8080/applications/localnews

##### check the status of the image newsbackend-integration

    kubectl get deployments -n newsbackend-integration-gitops news-backend -o yaml | grep image:

### Running the full GitOps workflow

##### Listing 5-30. Commands to trigger the GitOps Pipeline via the EventListener

    ## modify the EventListener to the new Pipeline    
    kubectl apply -f snippets/chapter5/gitops/EventListenerPushGitOps.yaml -n localnews-pipelines
    
    ## in case it is not active anymore - port-forward the EventListener    
    kubectl port-forward -n localnews-pipelines service/el-github-new-push-listener 9998:8080
    
    ## simulate the webhook triggering and modify with your sha
    curl -v \    
    -H 'X-GitHub-Event: push' \
    -H 'X-Hub-Signature: sha1=<**!!!recompute  with  the token 'would_come_from_your_github' which is also at  snippets/chapter5/github_push_listener/secret.yaml!!!**>' \
    -H 'Content-Type: application/json' \
    -d '{"ref": "refs/heads/main", "head_commit":{"id": "v1.0.0-your-fake-commit-id"}, "repository":{"clone_url":"<**!!!replace!!!**>"}, "image_repo":"<**!!!replace!!!**>"}' \
    http://localhost:9998

##### Track Progress of the Pipeline in the UI (which has to be port-forwarded on 9097 still)
    ## if not active anymore run:
    kubectl port-forward service/tekton-dashboard 9097:9097 -n tekton-pipelines
http://localhost:9097/#/namespaces/localnews-pipelines/pipelineruns

##### after successful PipelineRun check whether ArgoCD caught the freshly built Container Image and updated Helm Chart by inspected the actual state of the news-backend Deployment

    kubectl describe deployments -n newsbackend-integration-gitops  news-backend

### Cleaning Up

    minikube delete
    minikube start --addons=ingress --vm=true --kubernetes-version='v1.22.3' --memory='8g' --cpus='4' --disk-size='25000mb' 