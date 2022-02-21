package model

import (
	kubdevv1alpha1 "apress.com/m/v2/api/v1alpha1"
	v1 "k8s.io/api/core/v1"
	v12 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
)

func PostgisService(cr *kubdevv1alpha1.LocalNewsApp) *v1.Service {
	return &v1.Service{
		ObjectMeta: v12.ObjectMeta{
			Name:      "postgis",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": "postgis",
			},
		},
		Spec: getPostgisSvcSpec(cr),
	}
}

func getPostgisSvcSpec(cr *kubdevv1alpha1.LocalNewsApp) v1.ServiceSpec {
	spec := v1.ServiceSpec{}
	spec.Type = v1.ServiceTypeClusterIP
	spec.Selector = map[string]string{
		"app": "postgis",
	}
	if cr.Spec.Postgis.ServicePort < 1 {
		cr.Spec.Postgis.ServicePort = 5400
	}
	if cr.Spec.Postgis.ContainerPort < 1 {
		cr.Spec.Postgis.ContainerPort = 5432
	}
	spec.Ports = []v1.ServicePort{
		{
			Port:       cr.Spec.Postgis.ServicePort,
			TargetPort: intstr.FromInt(int(cr.Spec.Postgis.ContainerPort)),
		},
	}

	return spec
}

func ReconcilePostgisService(cr *kubdevv1alpha1.LocalNewsApp, currentState *v1.Service) *v1.Service {
	reconciled := currentState.DeepCopy()
	reconciled.Spec = getPostgisSvcSpec(cr)
	return reconciled
}
