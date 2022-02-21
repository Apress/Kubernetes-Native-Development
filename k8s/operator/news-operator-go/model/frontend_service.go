package model

import (
	kubdevv1alpha1 "apress.com/m/v2/api/v1alpha1"
	v1 "k8s.io/api/core/v1"
	v12 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
)

func FrontendService(cr *kubdevv1alpha1.LocalNewsApp) *v1.Service {
	return &v1.Service{
		ObjectMeta: v12.ObjectMeta{
			Name:      "news-frontend",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": "news-frontend",
			},
		},
		Spec: getFrontendSpec(cr),
	}
}

func getFrontendSpec(cr *kubdevv1alpha1.LocalNewsApp) v1.ServiceSpec {
	spec := v1.ServiceSpec{}
	spec.Type = v1.ServiceTypeNodePort
	spec.Selector = map[string]string{
		"app": "news-frontend",
	}
	if cr.Spec.NewsFrontend.ServicePort < 1 {
		cr.Spec.NewsFrontend.ServicePort = 80
	}
	if cr.Spec.NewsFrontend.ContainerPort < 1 {
		cr.Spec.NewsFrontend.ContainerPort = 80
	}
	if cr.Spec.NewsFrontend.NodePort < 1 {
		cr.Spec.NewsFrontend.NodePort = 31111
	}
	spec.Ports = []v1.ServicePort{
		{
			Port:       cr.Spec.NewsFrontend.ServicePort,
			TargetPort: intstr.FromInt(int(cr.Spec.NewsFrontend.ContainerPort)),
			NodePort:   cr.Spec.NewsFrontend.NodePort,
		},
	}

	return spec
}

func ReconcileFrontendService(cr *kubdevv1alpha1.LocalNewsApp, currentState *v1.Service) *v1.Service {
	reconciled := currentState.DeepCopy()
	reconciled.Spec = getFrontendSpec(cr)
	return reconciled
}
