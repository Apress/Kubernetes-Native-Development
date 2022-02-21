package model

import (
	kubdevv1alpha1 "apress.com/m/v2/api/v1alpha1"
	"fmt"
	v13 "k8s.io/api/apps/v1"
	v1 "k8s.io/api/core/v1"
	v12 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func BackendDeployment(cr *kubdevv1alpha1.LocalNewsApp) *v13.Deployment {
	return &v13.Deployment{
		ObjectMeta: v12.ObjectMeta{
			Name:      "news-backend",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": "news-backend",
			},
		},
		Spec: getBackendDeploymentSpec(cr),
	}
}

func getBackendDeploymentSpec(cr *kubdevv1alpha1.LocalNewsApp) v13.DeploymentSpec {
	if cr.Spec.NewsBackend.Image == "" {
		cr.Spec.NewsBackend.Image = "quay.io/k8snativedev/news-backend"
	}
	if cr.Spec.NewsBackend.ImageTag == "" {
		cr.Spec.NewsBackend.ImageTag = "latest"
	}
	if cr.Spec.NewsBackend.ReplicaCount < 1 {
		cr.Spec.NewsBackend.ReplicaCount = 1
	}
	if cr.Spec.NewsBackend.ContainerPort < 1 {
		cr.Spec.NewsBackend.ContainerPort = 8080
	}
	if cr.Spec.Extractor.ServicePort < 1 {
		cr.Spec.Extractor.ServicePort = 8081
	}
	if cr.Spec.NewsBackend.NlpUrl == "" {

		cr.Spec.NewsBackend.NlpUrl = fmt.Sprintf("http://location-extractor:%d/get_loc", cr.Spec.Extractor.ServicePort)
	}
	if cr.Spec.Postgis.ServicePort < 1 {
		cr.Spec.Postgis.ServicePort = 5400
	}
	if cr.Spec.NewsBackend.JdbcUrl == "" {
		cr.Spec.NewsBackend.JdbcUrl = fmt.Sprintf("jdbc:postgresql://postgis:%d/news?user=postgres&password=banane", cr.Spec.Postgis.ServicePort)
	}

	spec := v13.DeploymentSpec{
		Replicas: &cr.Spec.NewsBackend.ReplicaCount,
		Selector: &v12.LabelSelector{
			MatchLabels: map[string]string{
				"app": "news-backend",
			},
		},
		Template: v1.PodTemplateSpec{
			ObjectMeta: v12.ObjectMeta{
				Name:      "news-backend",
				Namespace: cr.Namespace,
				Labels: map[string]string{
					"app": "news-backend",
				},
			},
			Spec: v1.PodSpec{
				Containers: []v1.Container{
					{
						Name:  "news-backend",
						Image: cr.Spec.NewsBackend.Image,
						Ports: []v1.ContainerPort{
							{
								ContainerPort: cr.Spec.NewsBackend.ContainerPort,
								Protocol:      "TCP",
							},
						},
						Env: []v1.EnvVar{
							{
								Name:  "backend.nlp.url",
								Value: cr.Spec.NewsBackend.NlpUrl,
							},
							{
								Name:  "quarkus.datasource.jdbc.url",
								Value: cr.Spec.NewsBackend.JdbcUrl,
							},
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

func ReconcileBackendDeployment(cr *kubdevv1alpha1.LocalNewsApp, currentState *v13.Deployment) *v13.Deployment {
	reconciled := currentState.DeepCopy()
	reconciled.Spec = getBackendDeploymentSpec(cr)
	return reconciled
}
