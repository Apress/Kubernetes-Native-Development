# OpenShift Service Mesh
This tutorial describes how to deploy the local news application with network policies restricting traffic flows. This has been tested on OCP 4.10.

## Prerequisites 
The tutorial expects that you have already created a project, e.g. 'localnews' in OpenShift. Make sure to set the OCP_PROJECT and OCP_DOMAIN variables because they will be used throughout the following commands.

    OCP_PROJECT=localnews
    oc project $OCP_PROJECT
    OCP_USER=$(oc whoami)
    OCP_DOMAIN=$(oc whoami --show-server=true | sed -E 's/https:\/\/api\.|:6443//g')
    
Furthermore, make sure that you have istalled the OpenShift Service Mesh in your target OpenShift cluster by following the offical [docs](https://docs.openshift.com/container-platform/4.11/service_mesh/v2x/installing-ossm.html). 
    
    helm upgrade -i localnews k8s/helm-chart -f k8s/helm-chart/values-openshift.yaml \
    --set localnews.domain=$OCP_DOMAIN \
    --set localnews.networkpolicysecurity="on"
    


