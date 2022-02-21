curl -v \
-H 'X-GitHub-Event: push' \
-H 'X-Hub-Signature: sha1=f29c6f49a59048a1c4a2151a8857ea46769ea6d1' \
-H 'Content-Type: application/json' \
-d '{"ref": "refs/heads/main", "head_commit":{"id": "a99d19668565f80e0a911b9cb22ec5ef48a7e4e8"}, "repository":{"clone_url":"git@github.com:Apress/Kubernetes-Native-Development.git"}, "image_repo": "quay.io/k8snativedev"}' \
http://localhost:9998
