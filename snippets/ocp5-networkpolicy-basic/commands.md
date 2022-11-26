# NetworkPolicies
This tutorial describes how to deploy the local news application with network policies restricting traffic flows. This has been tested on OCP 4.10.

## Prerequisites 
The tutorial expects that you have already created a project, e.g. 'localnews' in OpenShift. Make sure to set the OCP_PROJECT and OCP_DOMAIN variables because they will be used throughout the following commands.

    OCP_PROJECT=localnews
    oc project $OCP_PROJECT
    OCP_DOMAIN=$(oc whoami --show-server=true | sed -E 's/https:\/\/api\.|:6443//g')
 
## Deploy the NetworkPolicies 
 
You can deploy the application with NetworkPolcicies that restricts communication to the minimum that is necessary to function correctly.
    
    helm upgrade -i localnews k8s/helm-chart -f k8s/helm-chart/values-openshift.yaml \
    --set localnews.domain=$OCP_DOMAIN \
    --set localnews.networkpolicysecurity="on"
    
## Verify the restricted access between components
You can, for example, try to access the news-backend from the new-frontend before and after deploying the NetworkPolicies. Note, that it is NOT necessary to have access from news-frontend to news-backend because both are accessed via your browser (JavaScript).

    oc exec deployment/news-frontend -- curl news-backend:8080 


