package model

import (
	kubdevv1alpha1 "apress.com/m/v2/api/v1alpha1"
	v1 "k8s.io/api/core/v1"
	v12 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
)

func BackendService(cr *kubdevv1alpha1.LocalNewsApp) *v1.Service {
	return &v1.Service{
		ObjectMeta: v12.ObjectMeta{
			Name:      "news-backend",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": "news-backend",
			},
		},
		Spec: getBackendSpec(cr),
	}
}

func getBackendSpec(cr *kubdevv1alpha1.LocalNewsApp) v1.ServiceSpec {
	spec := v1.ServiceSpec{}
	if cr.Spec.NewsFrontend.BackendConnection == "viaNodePort" || cr.Spec.NewsFrontend.BackendConnection == "" {
		spec.Type = v1.ServiceTypeNodePort
	} else {
		spec.Type = v1.ServiceTypeClusterIP
	}
	spec.Selector = map[string]string{
		"app": "news-backend",
	}
	if cr.Spec.NewsBackend.ServicePort < 1 {
		cr.Spec.NewsBackend.ServicePort = 8080
	}
	if cr.Spec.NewsBackend.ContainerPort < 1 {
		cr.Spec.NewsBackend.ContainerPort = 8080
	}
	if cr.Spec.NewsBackend.NodePort < 1 {
		cr.Spec.NewsBackend.NodePort = 30000
	}

	spec.Ports = []v1.ServicePort{
		{
			Port:       cr.Spec.NewsBackend.ServicePort,
			TargetPort: intstr.FromInt(int(cr.Spec.NewsBackend.ContainerPort)),
		},
	}
	if cr.Spec.NewsFrontend.BackendConnection == "viaNodePort" || cr.Spec.NewsFrontend.BackendConnection == "" {
		spec.Ports[0].NodePort = cr.Spec.NewsBackend.NodePort
	}
	return spec
}

func ReconcileBackendService(cr *kubdevv1alpha1.LocalNewsApp, currentState *v1.Service) *v1.Service {
	reconciled := currentState.DeepCopy()
	reconciled.Spec = getBackendSpec(cr)
	return reconciled
}
