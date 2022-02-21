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

package controllers

import (
	"context"

	kubdevv1alpha1 "apress.com/m/v2/api/v1alpha1"
	model "apress.com/m/v2/model"
	v13 "k8s.io/api/apps/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"
	ctrllog "sigs.k8s.io/controller-runtime/pkg/log"
)

// FeedAnalysisReconciler reconciles a FeedAnalysis object
type FeedAnalysisReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=kubdev.apress.com,resources=feedanalysis,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=kubdev.apress.com,resources=feedanalysis/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=kubdev.apress.com,resources=feedanalysis/finalizers,verbs=update

// Reconcile is part of the main kubernetes reconciliation loop which aims to
// move the current state of the cluster closer to the desired state.
// TODO(user): Modify the Reconcile function to compare the state specified by
// the FeedAnalysis object against the actual cluster state, and then
// perform operations to make the cluster state reflect the state specified by
// the user.
//
// For more details, check Reconcile and its Result here:
// - https://pkg.go.dev/sigs.k8s.io/controller-runtime@v0.10.0/pkg/reconcile
func (r *FeedAnalysisReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	_ = log.FromContext(ctx)

	// your logic here
	log := ctrllog.FromContext(ctx)
	log.Info("Reconciling")
	feedanalysis := &kubdevv1alpha1.FeedAnalysis{}
	err := r.Get(ctx, req.NamespacedName, feedanalysis)
	if err != nil {
		if errors.IsNotFound(err) {
			log.Info("Feedanalyis resource not found")
			return ctrl.Result{}, nil
		}
		log.Error(err, "Failed to get Feedanalyis")
		return ctrl.Result{}, err
	}
	err = r.reconcileFeedScraperDeployment(ctx, feedanalysis)
	if err != nil {
		log.Error(err, "Failed to reconcile feed scraper deployment")
		return ctrl.Result{}, err
	}
	err = r.Status().Update(ctx, feedanalysis)
	if err != nil {
		log.Error(err, "Failed to Update backend service")
		return ctrl.Result{}, err
	}
	return ctrl.Result{}, nil
}

func (r *FeedAnalysisReconciler) reconcileFeedScraperDeployment(ctx context.Context, feedanalysis *kubdevv1alpha1.FeedAnalysis) error {
	log := ctrllog.FromContext(ctx)
	found := &v13.Deployment{}

	app := &kubdevv1alpha1.LocalNewsApp{}
	app.Name = feedanalysis.Name
	app.Namespace = feedanalysis.Namespace
	app.Spec.FeedScraper = feedanalysis.Spec.FeedScraper
	app.Spec.FeedScraper.Name = feedanalysis.Name
	feedScraperDeployment := model.FeedScraperDeployment(app)
	err := r.Get(ctx, types.NamespacedName{Name: feedanalysis.Name, Namespace: feedanalysis.Namespace}, found)
	if app.Spec.FeedScraper.Deployment == "" || app.Spec.FeedScraper.Deployment == "on" {
		if err != nil && errors.IsNotFound(err) {
			log.Info("Not Found: Creating")
			ctrl.SetControllerReference(feedanalysis, feedScraperDeployment, r.Scheme)
			return r.Create(ctx, feedScraperDeployment)
		} else {
			log.Info("Found: Updating")
			reconciled := model.ReconcileFeedScraperDeployment(app, found)
			return r.Update(ctx, reconciled)
		}
	} else {
		if err == nil || !errors.IsNotFound(err) {
			log.Info("Found: But FeedScraper deployment is set to 'off' => deleting")
			return r.Delete(ctx, feedScraperDeployment)
		} else {
			log.Info("Not found: But FeedScraper deployment == 'off' => do nothing")
			return nil
		}
	}
}

// SetupWithManager sets up the controller with the Manager.
func (r *FeedAnalysisReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&kubdevv1alpha1.FeedAnalysis{}).
		Owns(&v13.Deployment{}).
		Complete(r)
}
