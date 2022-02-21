package model

import (
	kubdevv1alpha1 "apress.com/m/v2/api/v1alpha1"
	v13 "k8s.io/api/apps/v1"
	v1 "k8s.io/api/core/v1"
	v12 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"strings"
)

func FeedScraperDeployment(cr *kubdevv1alpha1.LocalNewsApp) *v13.Deployment {
	return &v13.Deployment{
		ObjectMeta: v12.ObjectMeta{
			Name:      cr.Spec.FeedScraper.Name,
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": "feed-scraper",
			},
		},
		Spec: getFeedScraperDeploymentSpec(cr),
	}
}

func getFeedScraperDeploymentSpec(cr *kubdevv1alpha1.LocalNewsApp) v13.DeploymentSpec {
	if cr.Spec.FeedScraper.Name == "" {
		cr.Spec.FeedScraper.Name = "feed-scraper"
	}
	if cr.Spec.FeedScraper.Image == "" {
		cr.Spec.FeedScraper.Image = "quay.io/k8snativedev/feed-scraper"
	}
	if cr.Spec.FeedScraper.ImageTag == "" {
		cr.Spec.FeedScraper.ImageTag = "latest"
	}
	if cr.Spec.FeedScraper.ReplicaCount < 1 {
		cr.Spec.FeedScraper.ReplicaCount = 1
	}
	if cr.Spec.FeedScraper.ContainerPort < 1 {
		cr.Spec.FeedScraper.ContainerPort = 8080
	}
	if cr.Spec.FeedScraper.FeedsUrl == nil {
		cr.Spec.FeedScraper.FeedsUrl = []string{"http://feeds.bbci.co.uk/news/world/rss.xml"}
	}
	if cr.Spec.FeedScraper.BackendHost == "" {
		cr.Spec.FeedScraper.BackendHost = "news-backend"
	}
	spec := v13.DeploymentSpec{
		Replicas: &cr.Spec.FeedScraper.ReplicaCount,
		Selector: &v12.LabelSelector{
			MatchLabels: map[string]string{
				"app": "feed-scraper",
			},
		},
		Template: v1.PodTemplateSpec{
			ObjectMeta: v12.ObjectMeta{
				Name:      "feed-scraper",
				Namespace: cr.Namespace,
				Labels: map[string]string{
					"app": "feed-scraper",
				},
			},
			Spec: v1.PodSpec{
				Containers: []v1.Container{
					{
						Name:  "feed-scraper",
						Image: cr.Spec.FeedScraper.Image,
						Ports: []v1.ContainerPort{
							{
								ContainerPort: cr.Spec.FeedScraper.ContainerPort,
								Protocol:      "TCP",
							},
						},
						Env: []v1.EnvVar{
							{
								Name:  "SCRAPER_FEEDS_URL",
								Value: strings.Join(cr.Spec.FeedScraper.FeedsUrl, ","),
							},
							{
								Name:  "SCRAPER_FEED_BACKEND_HOST",
								Value: cr.Spec.FeedScraper.BackendHost,
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

func ReconcileFeedScraperDeployment(cr *kubdevv1alpha1.LocalNewsApp, currentState *v13.Deployment) *v13.Deployment {
	reconciled := currentState.DeepCopy()
	reconciled.Spec = getFeedScraperDeploymentSpec(cr)
	return reconciled
}
