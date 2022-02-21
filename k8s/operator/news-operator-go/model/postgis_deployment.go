package model

import (
	kubdevv1alpha1 "apress.com/m/v2/api/v1alpha1"
	v13 "k8s.io/api/apps/v1"
	v1 "k8s.io/api/core/v1"
	v12 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func PostgisDeployment(cr *kubdevv1alpha1.LocalNewsApp) *v13.Deployment {
	return &v13.Deployment{
		ObjectMeta: v12.ObjectMeta{
			Name:      "postgis",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": "postgis",
			},
		},
		Spec: getPostgisDeploymentSpec(cr),
	}
}

func getPostgisDeploymentSpec(cr *kubdevv1alpha1.LocalNewsApp) v13.DeploymentSpec {
	if cr.Spec.Postgis.Image == "" {
		cr.Spec.Postgis.Image = "postgis/postgis:12-master"
	}
	if cr.Spec.Postgis.ContainerPort < 1 {
		cr.Spec.Postgis.ContainerPort = 5432
	}
	if cr.Spec.Postgis.ReplicaCount < 1 {
		cr.Spec.Postgis.ReplicaCount = 1
	}

	spec := v13.DeploymentSpec{
		Replicas: &cr.Spec.Postgis.ReplicaCount,
		Selector: &v12.LabelSelector{
			MatchLabels: map[string]string{
				"app": "postgis",
			},
		},
		Template: v1.PodTemplateSpec{
			ObjectMeta: v12.ObjectMeta{
				Name:      "postgis",
				Namespace: cr.Namespace,
				Labels: map[string]string{
					"app": "postgis",
				},
			},
			Spec: v1.PodSpec{
				Containers: []v1.Container{
					{
						Name:  "postgis",
						Image: cr.Spec.Postgis.Image,
						Ports: []v1.ContainerPort{
							{
								ContainerPort: cr.Spec.Postgis.ContainerPort,
								Protocol:      "TCP",
							},
						},
						Env: []v1.EnvVar{
							{
								Name:  "POSTGRES_USER",
								Value: "postgres",
							},
							{
								Name:  "POSTGRES_PASSWORD",
								Value: "banane",
							},
							{
								Name:  "POSTGRES_DB",
								Value: "news",
							},
							{
								Name:  "PGDATA",
								Value: "/tmp/pgdata",
							},
						},
					},
				},
			},
		},
		Strategy: v13.DeploymentStrategy{
			Type: v13.RecreateDeploymentStrategyType,
		},
	}
	return spec
}

func ReconcilePostgisDeployment(cr *kubdevv1alpha1.LocalNewsApp, currentState *v13.Deployment) *v13.Deployment {
	reconciled := currentState.DeepCopy()
	reconciled.Spec = getPostgisDeploymentSpec(cr)
	return reconciled
}
