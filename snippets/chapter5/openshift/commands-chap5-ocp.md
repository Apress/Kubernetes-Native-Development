# Commands Chapter 5

   ##### if you haven't done so, clone the git repo
    git clone https://github.com/sa-mw-dach/local-news-shift.git

## How do we install OpenShift Pipelines and OpenShift GitOps?

   ##### install Tekton
    oc apply -f snippets/chapter5/openshift/operators-subs/pipelines-operator-sub.yaml
    oc apply -f snippets/chapter5/openshift/operators-subs/gitops-operator-sub.yaml

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

##### Create the Pipeline
    kubectl apply -n localnews-pipelines -f snippets/chapter5/openshift/pipeline-resources/java-backend-simple-pipeline.yaml

##### Create a Secret to access your own Container Registry
   ###### Now, according to Listing 5-16, create an SSH Key and add the public key to your Github or any other SCM tool and the private Key to the secret at snippets/chapter5/rbac/github-secret.yaml 
   
##### create ClusterRole, ClusterRoleBinding, ServiceAccount and the Secret with the SSH key for the Git repo
    kubectl apply -n localnews-pipelines -f snippets/chapter5/openshift/rbac

##### Listing 5-18. Running the Pipeline with tkn
    ## Create your "Integration Environment" for news-backend
    oc new-project newsbackend-integration
    
    ## Start your Pipeline, thereby creating a PipelineRun
    oc create -f snippets/chapter5/openshift/pipeline-resources/java-backend-simple-pipeline-run.yaml

##### check out the progress of the PipelineRun via tkn

    tkn pipelinerun logs news-backend-simple-pipe-run-<your-id> -f -n localnews-pipelines

##### check out the progress of the PipelineRun via OpenShift GUI

### Triggering the Pipeline with a Git Push 

##### Listing 5-20. Create EventListener and associated resources
    kubectl apply -f snippets/chapter5/openshift/github_push_listener -n localnews-pipelines

##### Listing 5-21. Trigger the Pipeline with a simulated POST request

    kubectl port-forward -n localnews-pipelines service/el-github-new-push-listener 9998:8080
    
    curl -v \
    -H 'X-GitHub-Event: push' \
    -H 'X-Hub-Signature: sha1=d9340af41aff9ff9a63990bd42a316f304e6679a' \
    -H 'Content-Type: application/json' \
    -d '{"ref": "refs/heads/main", "head_commit":{"id": "a99d19668565f80e0a911b9cb22ec5ef48a7e4e8"}, "repository":{"clone_url":"git@github.com:sa-mw-dach/local-news-shift.git"}, "image_repo": "docker.io/maxisses"}' \
    http://localhost:9998

##### Listing 5-22. Generating an HMAC signature (UNIX command), go to  https://www.freeformatter.com/hmac-generator.html for UI based Generator
    echo -n '{"ref": "refs/heads/main", "head_commit":{"id": "a99d19668565f80e0a911b9cb22ec5ef48a7e4e8"}, "repository":{"clone_url":"git@github.com:sa-mw-dach/local-news-shift.git"}, "image_repo": "docker.io/maxisses"}' | openssl sha1 -hmac "would_come_from_your_github"

##### upon success check the application

    kubectl describe deployments -n newsbackend-integration news-backend
    minikube service news-frontend -n newsbackend-integration --url

##### clean up the integration environment for the next chapter to work

    kubectl delete namespace newsbackend-integration

## GitOps
### Adapting the Pipeline for GitOps

##### Listing 5-24. Deploy one additional Tasks to adapt the Pipeline for GitOps

    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/yq/0.2/yq.yaml

##### deploy the new Pipeline

    kubectl apply -n localnews-pipelines -f snippets/chapter5/openshift/pipeline-resources/java-backend-simple-pipeline-gitops.yaml

### Set up the GitOps Tool ArgoCD

##### Listing 5-26. Install ArgoCD
###### Navigate to the red hat applications menu icon menu → OpenShift GitOps → Cluster Argo CD. The login page of the Argo CD UI is displayed in a new window.

##### check whether Pods of ArgoCD are available

    oc get pods -n openshift-gitops

##### Listing 5-29. Commands to set up ArgoCD Application
    ## optional, when repo private: configure your Git repo, update the respective file with your own private SSH Key for e.g. Github
    kubectl apply -n openshift-gitops -f snippets/chapter5/openshift/gitops/argocd-repoaccess.yaml
    
    ## configure your Git repository
    kubectl apply -n openshift-gitops -f snippets/chapter5/openshift/gitops/argocd-application.yaml
    ## label your namespace to allow argo to manage and access it
    oc label namespace newsbackend-integration-gitops argocd.argoproj.io/managed-by=openshift-gitops

##### check the status of the application in Kubernetes

    kubectl describe application -n openshift-gitops localnews

##### check the status of the application in ArgoCD dashboard, once application is deployed and in sync

##### check the status of the image newsbackend-integration

    kubectl get deployments -n newsbackend-integration-gitops news-backend -o yaml | grep image:

### Running the full GitOps workflow

##### Listing 5-30. Commands to trigger the GitOps Pipeline via the EventListener

    ## modify the EventListener to the new Pipeline    
    kubectl apply -f snippets/chapter5/openshift/gitops/EventListenerPushGitOps.yaml -n localnews-pipelines
    
    ## in case it is not active anymore - port-forward the EventListener    
    kubectl port-forward -n localnews-pipelines service/el-github-new-push-listener 9998:8080

    ## build payload and sha1
    echo -n '{"ref": "refs/heads/main", "head_commit":{"id": "v1.0.0-your-fake-commit-id"}, "repository":{"clone_url":"git@github.com:sa-mw-dach/local-news-shift.git"}, "image_repo": "docker.io/maxisses"}' | openssl sha1 -hmac "would_come_from_your_github"
    
    ## simulate the webhook triggering and modify with your sha
    curl -v \
    -H 'X-GitHub-Event: push' \
    -H 'X-Hub-Signature: sha1=b60860d27da67ed1753b5a262c41f35b0c20dbcd' \
    -H 'Content-Type: application/json' \
    -d '{"ref": "refs/heads/main", "head_commit":{"id": "v1.0.0-your-fake-commit-id"}, "repository":{"clone_url":"git@github.com:sa-mw-dach/local-news-shift.git"}, "image_repo": "docker.io/maxisses"}' \
    http://localhost:9998

##### Track Progress of the Pipeline in the UI

##### after successful PipelineRun check whether ArgoCD caught the freshly built Container Image and updated Helm Chart by inspected the actual state of the news-backend Deployment

    kubectl describe deployments -n newsbackend-integration-gitops news-backend

