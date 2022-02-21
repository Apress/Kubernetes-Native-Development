package model

import (
	kubdevv1alpha1 "apress.com/m/v2/api/v1alpha1"
	"fmt"
	v13 "k8s.io/api/apps/v1"
	v1 "k8s.io/api/core/v1"
	v12 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func FrontendDeployment(cr *kubdevv1alpha1.LocalNewsApp) *v13.Deployment {
	return &v13.Deployment{
		ObjectMeta: v12.ObjectMeta{
			Name:      "news-frontend",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": "news-frontend",
			},
		},
		Spec: getFrontendDeploymentSpec(cr),
	}
}

func getFrontendDeploymentSpec(cr *kubdevv1alpha1.LocalNewsApp) v13.DeploymentSpec {
	if cr.Spec.NewsFrontend.Image == "" {
		cr.Spec.NewsFrontend.Image = "quay.io/k8snativedev/news-frontend"
	}
	if cr.Spec.NewsFrontend.ImageTag == "" {
		cr.Spec.NewsFrontend.ImageTag = "latest"
	}
	if cr.Spec.NewsFrontend.ReplicaCount < 1 {
		cr.Spec.NewsFrontend.ReplicaCount = 1
	}
	if cr.Spec.NewsFrontend.ContainerPort < 1 {
		cr.Spec.NewsFrontend.ContainerPort = 80
	}
	if cr.Spec.NewsFrontend.BackendConnection == "" {
		cr.Spec.NewsFrontend.BackendConnection = "viaNodePort"
	}
	if cr.Spec.NewsBackend.NodePort < 1 {
		cr.Spec.NewsBackend.NodePort = 30000
	}
	if cr.Spec.LocalNews.MinikubeIp == "" {
		cr.Spec.LocalNews.MinikubeIp = "fill-in-minikube-ip"
	}
	if cr.Spec.LocalNews.Domain == "" {
		cr.Spec.LocalNews.Domain = "nip.io"
	}

	nodeIpEntry := v1.EnvVar{
		Name: "NODE_IP",
		ValueFrom: &v1.EnvVarSource{
			FieldRef: &v1.ObjectFieldSelector{
				FieldPath: "status.hostIP",
			},
		},
	}
	backendPort := cr.Spec.NewsBackend.NodePort
	if cr.Spec.NewsFrontend.BackendConnection == "viaIngress" {
		nodeIpEntry = v1.EnvVar{
			Name:  "NODE_IP",
			Value: fmt.Sprintf("news-backend.%s.%s", cr.Spec.LocalNews.MinikubeIp, cr.Spec.LocalNews.Domain),
		}
		backendPort = 80
	}

	spec := v13.DeploymentSpec{
		Replicas: &cr.Spec.NewsFrontend.ReplicaCount,
		Selector: &v12.LabelSelector{
			MatchLabels: map[string]string{
				"app": "news-frontend",
			},
		},
		Template: v1.PodTemplateSpec{
			ObjectMeta: v12.ObjectMeta{
				Name:      "news-frontend",
				Namespace: cr.Namespace,
				Labels: map[string]string{
					"app": "news-frontend",
				},
			},
			Spec: v1.PodSpec{
				Containers: []v1.Container{
					{
						Name:  "news-frontend",
						Image: cr.Spec.NewsFrontend.Image,
						Ports: []v1.ContainerPort{
							{
								ContainerPort: cr.Spec.NewsFrontend.ContainerPort,
								Protocol:      "TCP",
							},
						},
						Env: []v1.EnvVar{
							{
								Name:  "NODE_PORT",
								Value: fmt.Sprintf("%d", backendPort),
							}, nodeIpEntry,
						},
					},
				},
			},
		},
		Strategy: v13.DeploymentStrategy{
			Type: v13.RollingUpdateDeploymentStrategyType,
		},
	}
	return spec
}

func ReconcileFrontendDeployment(cr *kubdevv1alpha1.LocalNewsApp, currentState *v13.Deployment) *v13.Deployment {
	reconciled := currentState.DeepCopy()
	reconciled.Spec = getFrontendDeploymentSpec(cr)
	return reconciled
}
