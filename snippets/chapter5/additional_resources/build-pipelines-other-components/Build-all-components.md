# This Readme is guiding you to build and push all components of the Local News Application from scratch

## How do we install Tekton?

##### install Tekton
    kubectl apply --filename https://storage.googleapis.com/tekton-releases/pipeline/previous/v0.30.0/release.yaml
##### check the status
    kubectl get pods --namespace tekton-pipelines
##### install the Dashboard
    kubectl apply --filename https://storage.googleapis.com/tekton-releases/dashboard/previous/v0.22.0/tekton-dashboard-release.yaml


## Create all Tasks
    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/maven/0.2/maven.yaml
    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/git-clone/0.5/git-clone.yaml
    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/buildah/0.2/buildah.yaml
    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/helm-upgrade-from-source/0.3/helm-upgrade-from-source.yaml
    kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/npm/0.1/npm.yaml

## Create Namespace
    kubectl create ns localnews-pipelines
## set Up Access and Permissions
#### registry access
    kubectl create secret docker-registry k8snativedev-tekton-pull-secret -n localnews-pipelines --docker-server=<your-registry-eg-docker.io-or-quay.io> --docker-username=<your-name> --docker-password=<your-password>
##### Patch the Secret so Tekton recognizes it

    ## replace "docker.io" with your own Container Registry
    kubectl patch secret k8snativedev-tekton-pull-secret -n localnews-pipelines -p '{"metadata":{"annotations":{"tekton.dev/docker-0": "https://quay.io"}}}'

##### create ClusterRole, ClusterRoleBinding, ServiceAccount and the Secret with the SSH key for the Git repo
    kubectl apply -n localnews-pipelines -f snippets/chapter5/rbac

#### Create the 4 Pipeline Resources ONLY for building and pushing the Container Images
    kubectl apply -f snippets/chapter5/additional_resources/build-pipelines-other-components -n localnews-pipelines

## run pipelines
    ##run the script from root, edit repo and container registry accordingly:
    ./snippets/chapter5/additional_resources/build-pipelines-other-components/build-all.sh

#### check out the progress of the PipelineRun via Tekton Dashboard

    kubectl port-forward service/tekton-dashboard 9097:9097 -n tekton-pipelines
    ## now head over to your browser at:
   [http://localhost:9097/#/namespaces/localnews-pipelines/pipelineruns](http://localhost:9097/#/namespaces/localnews-pipelines/pipelineruns)

