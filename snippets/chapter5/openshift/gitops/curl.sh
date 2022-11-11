curl -v \
-H 'X-GitHub-Event: push' \
-H 'X-Hub-Signature: sha1=c78aa6488f850aaedaba34d60d2a880ef7172b98' \
-H 'Content-Type: application/json' \
-d '{"ref": "refs/heads/main", "head_commit":{"id": "v1.0.0-your-fake-commit-id"}, "repository":{"clone_url":"git@github.com:Apress/Kubernetes-Native-Development.git"}, "image_repo":"quay.io/k8snativedev"}' \
http://localhost:9998

curl -v \
-H 'X-GitHub-Event: push' \
-H 'X-Hub-Signature: sha1=5fe92d8bf264873e0b167a27b4e8856680dc8935' \
-H 'Content-Type: application/json' \
-d '{"ref": "refs/heads/main", "head_commit":{"id": "v1.0.0-your-fake-commit-id"}, "repository":{"clone_url":"git@github.com:Javatar81/localnews.git"}, "image_repo":"docker.io/maxisses"}' \
http://localhost:9998
