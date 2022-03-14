export GIT_REPO=https://github.com/Apress/Kubernetes-Native-Development.git
export CONTAINER_IMAGE_REPO=quay.io/k8snativedev

###Location-Extractor build Pipe
tkn pipeline start -n localnews-pipelines location-extractor-build-pipe \
    --workspace name=shared-workspace,volumeClaimTemplateFile=snippets/chapter5/persistence/volumeclaimtemplate.yaml \
    --serviceaccount=clone-and-build-bot \
    --param gitrepositoryurl=$GIT_REPO \
    --param image_repo=$CONTAINER_IMAGE_REPO \
    --param component=location_extractor \
    --param image_version=latest \
    --use-param-defaults 

###Java Feedscraper FULL build Pipe
tkn pipeline start -n localnews-pipelines feed-scraper-build-pipe \
    --workspace name=shared-workspace,volumeClaimTemplateFile=snippets/chapter5/persistence/volumeclaimtemplate.yaml \
    --workspace name=maven-settings,emptyDir="" \
    --serviceaccount=clone-and-build-bot \
    --param gitrepositoryurl=$GIT_REPO \
    --param image_repo=$CONTAINER_IMAGE_REPO \
    --param component=feed-scraper \
    --param image_version=latest \
    --use-param-defaults 

###Java Feedscraper PURE/JOB build Pipe
tkn pipeline start -n localnews-pipelines feed-scraper-job-build-pipe \
    --workspace name=shared-workspace,volumeClaimTemplateFile=snippets/chapter5/persistence/volumeclaimtemplate.yaml \
    --workspace name=maven-settings,emptyDir="" \
    --serviceaccount=clone-and-build-bot \
    --param gitrepositoryurl=$GIT_REPO \
    --param image_repo=$CONTAINER_IMAGE_REPO \
    --param component=feed-scraper \
    --param component-sub=pure \
    --param image_version=job \
    --use-param-defaults 

###News-Frontend Buíld Pipe
tkn pipeline start -n localnews-pipelines news-frontend-build-pipe \
    --workspace name=shared-workspace,volumeClaimTemplateFile=snippets/chapter5/persistence/volumeclaimtemplate.yaml \
    --serviceaccount=clone-and-build-bot \
    --param gitrepositoryurl=$GIT_REPO \
    --param image_repo=$CONTAINER_IMAGE_REPO \
    --param component=news-frontend \
    --param image_version=latest \
    --use-param-defaults 

###Quarkus News-Backend build Pipe
tkn pipeline start -n localnews-pipelines news-backend-build-pipe \
    --workspace name=shared-workspace,volumeClaimTemplateFile=snippets/chapter5/persistence/volumeclaimtemplate.yaml \
    --workspace name=maven-settings,emptyDir="" \
    --serviceaccount=clone-and-build-bot \
    --param gitrepositoryurl=$GIT_REPO \
    --param image_repo=$CONTAINER_IMAGE_REPO \
    --param component=news-backend \
    --param image_version=latest \
    --use-param-defaults 

###Quarkus News-Backend Mutable Jar build Pipe (to start container e.g. in Dev mode)
tkn pipeline start -n localnews-pipelines news-backend-build-pipe \
    --workspace name=shared-workspace,volumeClaimTemplateFile=snippets/chapter5/persistence/volumeclaimtemplate.yaml \
    --workspace name=maven-settings,emptyDir="" \
    --serviceaccount=clone-and-build-bot \
    --param gitrepositoryurl=$GIT_REPO \
    --param image_repo=$CONTAINER_IMAGE_REPO \
    --param component=news-backend \
    --param image_version=latest-dev \
    --param mvn_options='clean,package,-DskipTests=true,-Dquarkus.package.type=mutable-jar' \
    --use-param-defaults