package model

import (
	kubdevv1alpha1 "apress.com/m/v2/api/v1alpha1"
	v1 "k8s.io/api/core/v1"
	v12 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
)

func LocationExtractorService(cr *kubdevv1alpha1.LocalNewsApp) *v1.Service {
	return &v1.Service{
		ObjectMeta: v12.ObjectMeta{
			Name:      "location-extractor",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": "location-extractor",
			},
		},
		Spec: getPostgisSvcSpec(cr),
	}
}

func getLocationExtractorSvcSpec(cr *kubdevv1alpha1.LocalNewsApp) v1.ServiceSpec {
	spec := v1.ServiceSpec{}
	spec.Type = v1.ServiceTypeClusterIP
	spec.Selector = map[string]string{
		"app": "location-extractor",
	}
	if cr.Spec.Extractor.ServicePort < 1 {
		cr.Spec.Extractor.ServicePort = 8081
	}
	if cr.Spec.Extractor.ContainerPort < 1 {
		cr.Spec.Extractor.ContainerPort = 80
	}
	spec.Ports = []v1.ServicePort{
		{
			Port:       cr.Spec.Extractor.ServicePort,
			TargetPort: intstr.FromInt(int(cr.Spec.Extractor.ContainerPort)),
		},
	}

	return spec
}

func ReconcileLocationExtractorService(cr *kubdevv1alpha1.LocalNewsApp, currentState *v1.Service) *v1.Service {
	reconciled := currentState.DeepCopy()
	reconciled.Spec = getLocationExtractorSvcSpec(cr)
	return reconciled
}
