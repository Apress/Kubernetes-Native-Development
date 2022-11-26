# Deploy the Local News Operator to OpenShift
This tutorial describes how to deploy the local news operator to OpenShift. This has been tested on OCP 4.10.

## Clone the repository
    git clone https://github.com/Apress/Kubernetes-Native-Development

## Create a new project
    OCP_PROJECT=localnews-operator
    oc new-project $OCP_PROJECT
    OCP_USER=$(oc whoami)
    
## RBAC considerations for non-cluster-admins
Create the necessary roles to manage the CRDs. This step requires the permission to create ClusterRoles. You can skip this when you are going to use a user with cluster admin permissions.

    oc login <admin>
    oc apply -f snippets/ocp6-operator/crd-role.yaml
    cd k8s/operator/news-operator-helm
    oc apply -f config/rbac/role.yaml
    oc adm policy add-cluster-role-to-user manager-role $OCP_USER
    oc adm policy add-cluster-role-to-user crd-role $OCP_USER
    
## Run the operator locally
Run the operator locally with the make command and create a new local news CR.
      
    make install run
    # In another terminal tab from the git root folder
    oc apply -f snippets/ocp6-operator/localnews.yaml 