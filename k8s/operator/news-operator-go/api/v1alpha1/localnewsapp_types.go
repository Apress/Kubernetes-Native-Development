/*
Copyright 2021.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// LocalNewsAppSpec defines the desired state of LocalNewsApp
type LocalNewsAppSpec struct {
	// INSERT ADDITIONAL SPEC FIELDS - desired state of cluster
	// Important: Run "make" to regenerate code after modifying this file

	NewsBackend  NewsBackend       `json:"newsbackend,omitempty"`
	FeedScraper  FeedScraper       `json:"feedscraper,omitempty"`
	NewsFrontend NewsFrontend      `json:"newsfrontend,omitempty"`
	Postgis      Postgis           `json:"postgis,omitempty"`
	Extractor    LocationExtractor `json:"locationextractor,omitempty"`
	LocalNews    LocalNews         `json:"localnews,omitempty"`
}

type LocalNews struct {
	MinikubeIp string `json:"minikubeIp,omitempty"`
	// +kubebuilder:default:="nip.io"
	Domain string `json:"domain,omitempty"`
}

type FeedScraper struct {
	// +kubebuilder:default:="feed-scraper"
	Name string `json:"name,omitempty"`
	// +kubebuilder:default:="on"
	Deployment string `json:"deployment,omitempty"`
	// +kubebuilder:default:=8080
	ContainerPort int32 `json:"containerPort,omitempty"`
	// +kubebuilder:default:="quay.io/k8snativedev/feed-scraper"
	Image string `json:"image,omitempty"`
	// +kubebuilder:default:="latest"
	ImageTag string `json:"imageTag,omitempty"`
	// +kubebuilder:default:=1
	ReplicaCount int32 `json:"replicaCount,omitempty"`
	// +kubebuilder:default:="news-backend"
	BackendHost string `json:"backendHost,omitempty"`
	// +kubebuilder:default:={"http://feeds.bbci.co.uk/news/world/rss.xml"}
	FeedsUrl []string `json:"feedsUrl,omitempty"`
}

type NewsFrontend struct {
	// +kubebuilder:default:="on"
	Deployment string `json:"deployment,omitempty"`
	// +kubebuilder:default:=80
	ServicePort int32 `json:"servicePort,omitempty"`
	// +kubebuilder:default:=80
	ContainerPort int32 `json:"containerPort,omitempty"`
	// +kubebuilder:default:=31111
	NodePort int32 `json:"nodePort,omitempty"`
	// +kubebuilder:default:="quay.io/k8snativedev/news-frontend"
	Image string `json:"image,omitempty"`
	// +kubebuilder:default:="latest"
	ImageTag string `json:"imageTag,omitempty"`
	// +kubebuilder:default:=1
	ReplicaCount int32 `json:"replicaCount,omitempty"`
	// +kubebuilder:default:="viaNodePort"
	BackendConnection string `json:"backendConnection,omitempty"`
}

type LocationExtractor struct {
	// +kubebuilder:default:="on"
	Deployment string `json:"deployment,omitempty"`
	// +kubebuilder:default:=8081
	ServicePort int32 `json:"servicePort,omitempty"`
	// +kubebuilder:default:=80
	ContainerPort int32 `json:"containerPort,omitempty"`
	// +kubebuilder:default:="quay.io/k8snativedev/location_extractor"
	Image string `json:"image,omitempty"`
	// +kubebuilder:default:="latest"
	ImageTag string `json:"imageTag,omitempty"`
	// +kubebuilder:default:=1
	ReplicaCount int32 `json:"replicaCount,omitempty"`
}

type NewsBackend struct {
	// +kubebuilder:default:="on"
	Deployment string `json:"deployment,omitempty"`
	// +kubebuilder:default:=8080
	ServicePort int32 `json:"servicePort,omitempty"`
	// +kubebuilder:default:=8080
	ContainerPort int32 `json:"containerPort,omitempty"`
	// +kubebuilder:default:=30000
	NodePort int32 `json:"nodePort,omitempty"`
	// +kubebuilder:default:="quay.io/k8snativedev/news-backend"
	Image string `json:"image,omitempty"`
	// +kubebuilder:default:="latest"
	ImageTag string `json:"imageTag,omitempty"`
	// +kubebuilder:default:=1
	ReplicaCount int32 `json:"replicaCount,omitempty"`
	// +kubebuilder:default:="http://location-extractor:8081/get_loc"
	NlpUrl string `json:"nlpUrl,omitempty"`
	// +kubebuilder:default:="jdbc:postgresql://postgis:5400/news?user=postgres&password=banane"
	JdbcUrl string `json:"jdbcUrl,omitempty"`
}

type Postgis struct {
	// +kubebuilder:default:=5432
	ServicePort int32 `json:"servicePort,omitempty"`
	// +kubebuilder:default:=5400
	ContainerPort int32 `json:"containerPort,omitempty"`
	// +kubebuilder:default:="postgis/postgis:12-master"
	Image string `json:"image,omitempty"`
	// +kubebuilder:default:=1
	ReplicaCount int32 `json:"replicaCount,omitempty"`
}

// LocalNewsAppStatus defines the observed state of LocalNewsApp
type LocalNewsAppStatus struct {
	// INSERT ADDITIONAL STATUS FIELD - define observed state of cluster
	// Important: Run "make" to regenerate code after modifying this file
	// +kubebuilder:default:={"not deployed"}
	ManagedResources []string `json:"managedResources,omitempty"`
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status

// LocalNewsApp is the Schema for the localnewsapps API
type LocalNewsApp struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   LocalNewsAppSpec   `json:"spec,omitempty"`
	Status LocalNewsAppStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// LocalNewsAppList contains a list of LocalNewsApp
type LocalNewsAppList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []LocalNewsApp `json:"items"`
}

func init() {
	SchemeBuilder.Register(&LocalNewsApp{}, &LocalNewsAppList{})
}
