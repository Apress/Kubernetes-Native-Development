while [ $(oc get deployments -n newsbackend-integration-gitops news-backend -o jsonpath="{.spec.template.spec.containers[:1].image}") != "docker.io/maxisses/news-backend:latest" ]
do
  echo "Waiting for successful ArgoCD sync - checking in another 3 seconds"
  sleep 3
done