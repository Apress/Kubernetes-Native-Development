package model

import (
	kubdevv1alpha1 "apress.com/m/v2/api/v1alpha1"
	v13 "k8s.io/api/apps/v1"
	v1 "k8s.io/api/core/v1"
	v12 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func LocationExtractorDeployment(cr *kubdevv1alpha1.LocalNewsApp) *v13.Deployment {
	return &v13.Deployment{
		ObjectMeta: v12.ObjectMeta{
			Name:      "location-extractor",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": "location-extractor",
			},
		},
		Spec: getLocationExtractorDeploymentSpec(cr),
	}
}

func getLocationExtractorDeploymentSpec(cr *kubdevv1alpha1.LocalNewsApp) v13.DeploymentSpec {
	if cr.Spec.Extractor.Image == "" {
		cr.Spec.Extractor.Image = "quay.io/k8snativedev/location_extractor"
	}
	if cr.Spec.Extractor.ImageTag == "" {
		cr.Spec.Extractor.ImageTag = "latest"
	}
	if cr.Spec.Extractor.ContainerPort < 1 {
		cr.Spec.Extractor.ContainerPort = 80
	}
	if cr.Spec.Extractor.ReplicaCount < 1 {
		cr.Spec.Extractor.ReplicaCount = 1
	}

	spec := v13.DeploymentSpec{
		Replicas: &cr.Spec.Extractor.ReplicaCount,
		Selector: &v12.LabelSelector{
			MatchLabels: map[string]string{
				"app": "location-extractor",
			},
		},
		Template: v1.PodTemplateSpec{
			ObjectMeta: v12.ObjectMeta{
				Name:      "location-extractor",
				Namespace: cr.Namespace,
				Labels: map[string]string{
					"app": "location-extractor",
				},
			},
			Spec: v1.PodSpec{
				Containers: []v1.Container{
					{
						Name:  "location-extractor",
						Image: cr.Spec.Extractor.Image,
						Ports: []v1.ContainerPort{
							{
								ContainerPort: cr.Spec.Extractor.ContainerPort,
								Protocol:      "TCP",
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

func ReconcileLocationExtractorDeployment(cr *kubdevv1alpha1.LocalNewsApp, currentState *v13.Deployment) *v13.Deployment {
	reconciled := currentState.DeepCopy()
	reconciled.Spec = getLocationExtractorDeploymentSpec(cr)
	return reconciled
}
