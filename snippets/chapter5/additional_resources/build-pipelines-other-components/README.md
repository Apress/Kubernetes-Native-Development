# ensure all Resources from folder snippets/chapter5/rbac are applied with your git(if own repo and private) and Container Registry Auth
# Install additional Tekton Catalog Tasks
kubectl apply -n localnews-pipelines -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/npm/0.1/npm.yaml
# Create the 4 Pipeline Resources ONLY for building and pushing the Container Images
kubectl apply -f snippets/chapter5/additional_resources/build-pipelines-other-components -n localnews-pipelines


# run pipelines
run the script from root:
./snippets/chapter5/additional_resources/build-pipelines-other-components/build-all.sh