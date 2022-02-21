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
	v1 "k8s.io/api/core/v1"
	networkingv1 "k8s.io/api/networking/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	ctrllog "sigs.k8s.io/controller-runtime/pkg/log"
)

// LocalNewsAppReconciler reconciles a LocalNewsApp object
type LocalNewsAppReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=kubdev.apress.com,resources=localnewsapps,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=kubdev.apress.com,resources=localnewsapps/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=kubdev.apress.com,resources=localnewsapps/finalizers,verbs=update

// Reconcile is part of the main kubernetes reconciliation loop which aims to
// move the current state of the cluster closer to the desired state.
// TODO(user): Modify the Reconcile function to compare the state specified by
// the LocalNewsApp object against the actual cluster state, and then
// perform operations to make the cluster state reflect the state specified by
// the user.
//
// For more details, check Reconcile and its Result here:
// - https://pkg.go.dev/sigs.k8s.io/controller-runtime@v0.10.0/pkg/reconcile
func (r *LocalNewsAppReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	// your logic here
	log := ctrllog.FromContext(ctx)
	log.Info("Reconciling")
	localnewsapp := &kubdevv1alpha1.LocalNewsApp{}
	err := r.Get(ctx, req.NamespacedName, localnewsapp)
	if err != nil {
		if errors.IsNotFound(err) {
			log.Info("LocalNewsApp resource not found")
			return ctrl.Result{}, nil
		}
		log.Error(err, "Failed to get LocalNewsApp")
		return ctrl.Result{}, err
	}
	if localnewsapp.Status.ManagedResources == nil {
		localnewsapp.Status.ManagedResources = []string{"Postigs Deployment Not Ready", "Postigs Service Not Ready",
			"Backend Deployment Not Ready", "Backend Service Not Ready", "Location Extractor Deployment Not Ready", "Location Extractor Service Not Ready",
			"FeedScraper Deployment Not Ready", "Frontend Deployment Not Ready", "Frontend Service Not Ready"}
	}
	err = r.reconcilePostgisService(ctx, localnewsapp)
	if err != nil {
		log.Error(err, "Failed to reconcile postgis service")
		return ctrl.Result{}, err
	}
	err = r.reconcilePostgisDeployment(ctx, localnewsapp)
	if err != nil {
		log.Error(err, "Failed to reconcile postgis deployment")
		return ctrl.Result{}, err
	}

	err = r.reconcileBackendService(ctx, localnewsapp)
	if err != nil {
		log.Error(err, "Failed to reconcile backend service")
		return ctrl.Result{}, err
	}
	err = r.reconcileBackendDeployment(ctx, localnewsapp)
	if err != nil {
		log.Error(err, "Failed to reconcile backend deployment")
		return ctrl.Result{}, err
	}
	if localnewsapp.Spec.NewsFrontend.BackendConnection == "viaIngress" {
		err = r.reconcileBackendIngress(ctx, localnewsapp)
		if err != nil {
			log.Error(err, "Failed to reconcile backend ingress")
			return ctrl.Result{}, err
		}
	}
	err = r.reconcileLocationExtractorService(ctx, localnewsapp)
	if err != nil {
		log.Error(err, "Failed to reconcile location extractor service")
		return ctrl.Result{}, err
	}
	err = r.reconcileLocationExtractorDeployment(ctx, localnewsapp)
	if err != nil {
		log.Error(err, "Failed to reconcile location extractor deployment")
		return ctrl.Result{}, err
	}
	err = r.reconcileFrontendService(ctx, localnewsapp)
	if err != nil {
		log.Error(err, "Failed to reconcile frontend service")
		return ctrl.Result{}, err
	}
	err = r.reconcileFrontendDeployment(ctx, localnewsapp)
	if err != nil {
		log.Error(err, "Failed to reconcile frontend service")
		return ctrl.Result{}, err
	}
	err = r.reconcileFrontendIngress(ctx, localnewsapp)
	if err != nil {
		log.Error(err, "Failed to reconcile frontend ingress")
		return ctrl.Result{}, err
	}

	err = r.reconcileFeedScraperDeployment(ctx, localnewsapp)
	if err != nil {
		log.Error(err, "Failed to reconcile feed scraper deployment")
		return ctrl.Result{}, err
	}

	err = r.Status().Update(ctx, localnewsapp)
	if err != nil {
		log.Error(err, "Failed to Update backend service")
		return ctrl.Result{}, err
	}
	return ctrl.Result{}, nil
}

func (r *LocalNewsAppReconciler) reconcilePostgisDeployment(ctx context.Context, app *kubdevv1alpha1.LocalNewsApp) error {
	log := ctrllog.FromContext(ctx)
	found := &v13.Deployment{}
	postgisDeployment := model.PostgisDeployment(app)
	err := r.Get(ctx, types.NamespacedName{Name: postgisDeployment.Name, Namespace: app.Namespace}, found)
	app.Status.ManagedResources[0] = "deployment/" + postgisDeployment.Name
	if err != nil && errors.IsNotFound(err) {
		log.Info("Not Found: Creating")
		ctrl.SetControllerReference(app, postgisDeployment, r.Scheme)
		return r.Create(ctx, postgisDeployment)
	} else {
		log.Info("Found: Updating")
		reconciled := model.ReconcilePostgisDeployment(app, found)
		return r.Update(ctx, reconciled)
	}
}

func (r *LocalNewsAppReconciler) reconcilePostgisService(ctx context.Context, app *kubdevv1alpha1.LocalNewsApp) error {
	log := ctrllog.FromContext(ctx)
	found := &v1.Service{}
	postgisService := model.PostgisService(app)
	err := r.Get(ctx, types.NamespacedName{Name: postgisService.Name, Namespace: app.Namespace}, found)
	app.Status.ManagedResources[1] = "svc/" + postgisService.Name
	if err != nil && errors.IsNotFound(err) {
		log.Info("Not Found: Creating")
		ctrl.SetControllerReference(app, postgisService, r.Scheme)
		return r.Create(ctx, postgisService)
	} else {
		log.Info("Found: Updating")
		reconciled := model.ReconcilePostgisService(app, found)
		return r.Update(ctx, reconciled)
	}
}

func (r *LocalNewsAppReconciler) reconcileBackendDeployment(ctx context.Context, app *kubdevv1alpha1.LocalNewsApp) error {
	log := ctrllog.FromContext(ctx)
	found := &v13.Deployment{}
	backendDeployment := model.BackendDeployment(app)
	err := r.Get(ctx, types.NamespacedName{Name: backendDeployment.Name, Namespace: app.Namespace}, found)
	app.Status.ManagedResources[2] = "deployment/" + backendDeployment.Name
	if app.Spec.NewsBackend.Deployment == "" || app.Spec.NewsBackend.Deployment == "on" {
		if err != nil && errors.IsNotFound(err) {
			log.Info("Not Found: Creating")
			ctrl.SetControllerReference(app, backendDeployment, r.Scheme)
			return r.Create(ctx, backendDeployment)
		} else {
			log.Info("Found: Updating")
			reconciled := model.ReconcileBackendDeployment(app, found)
			return r.Update(ctx, reconciled)
		}
	} else {
		if err == nil || !errors.IsNotFound(err) {
			log.Info("Found: But Backend deployment is set to 'off' => deleting")
			return r.Delete(ctx, backendDeployment)
		} else {
			log.Info("Not found: But Backend deployment == 'off' => do nothing")
			return nil
		}
	}
}

func (r *LocalNewsAppReconciler) reconcileBackendService(ctx context.Context, app *kubdevv1alpha1.LocalNewsApp) error {
	log := ctrllog.FromContext(ctx)
	found := &v1.Service{}
	backendService := model.BackendService(app)
	err := r.Get(ctx, types.NamespacedName{Name: backendService.Name, Namespace: app.Namespace}, found)
	app.Status.ManagedResources[3] = "svc/" + backendService.Name
	if app.Spec.NewsBackend.Deployment == "" || app.Spec.NewsBackend.Deployment == "on" {
		if err != nil && errors.IsNotFound(err) {
			log.Info("Not Found: Creating")
			ctrl.SetControllerReference(app, backendService, r.Scheme)
			return r.Create(ctx, backendService)
		} else {
			log.Info("Found: Updating")
			reconciled := model.ReconcileBackendService(app, found)
			return r.Update(ctx, reconciled)
		}
	} else {
		if err == nil || !errors.IsNotFound(err) {
			log.Info("Found: But Backend service is set to 'off' => deleting")
			return r.Delete(ctx, backendService)
		} else {
			log.Info("Not found: But Backend service == 'off' => do nothing")
			return nil
		}
	}
}

func (r *LocalNewsAppReconciler) reconcileBackendIngress(ctx context.Context, app *kubdevv1alpha1.LocalNewsApp) error {
	log := ctrllog.FromContext(ctx)
	found := &networkingv1.Ingress{}
	backendIngress := model.BackendIngress(app)
	err := r.Get(ctx, types.NamespacedName{Name: backendIngress.Name, Namespace: app.Namespace}, found)
	if app.Spec.NewsBackend.Deployment == "" || app.Spec.NewsBackend.Deployment == "on" {
		if err != nil && errors.IsNotFound(err) {
			log.Info("Not Found: Creating")
			ctrl.SetControllerReference(app, backendIngress, r.Scheme)
			return r.Create(ctx, backendIngress)
		} else {
			log.Info("Found: Updating")
			reconciled := model.ReconcileBackendIngress(app, found)
			return r.Update(ctx, reconciled)
		}
	} else {
		if err == nil || !errors.IsNotFound(err) {
			log.Info("Found: But Backend service is set to 'off' => deleting")
			return r.Delete(ctx, backendIngress)
		} else {
			log.Info("Not found: But Backend service == 'off' => do nothing")
			return nil
		}
	}
}

func (r *LocalNewsAppReconciler) reconcileLocationExtractorDeployment(ctx context.Context, app *kubdevv1alpha1.LocalNewsApp) error {
	log := ctrllog.FromContext(ctx)
	found := &v13.Deployment{}
	locationExtractorDeployment := model.LocationExtractorDeployment(app)
	err := r.Get(ctx, types.NamespacedName{Name: locationExtractorDeployment.Name, Namespace: app.Namespace}, found)
	app.Status.ManagedResources[4] = "deployment/" + locationExtractorDeployment.Name
	if app.Spec.Extractor.Deployment == "" || app.Spec.Extractor.Deployment == "on" {
		if err != nil && errors.IsNotFound(err) {
			log.Info("Not Found: Creating")
			ctrl.SetControllerReference(app, locationExtractorDeployment, r.Scheme)
			return r.Create(ctx, locationExtractorDeployment)
		} else {
			log.Info("Found: Updating")
			reconciled := model.ReconcileLocationExtractorDeployment(app, found)
			return r.Update(ctx, reconciled)
		}
	} else {
		if err == nil || !errors.IsNotFound(err) {
			log.Info("Found: But LocationExtractor deployment is set to 'off' => deleting")
			return r.Delete(ctx, locationExtractorDeployment)
		} else {
			log.Info("Not found: But LocationExtractor deployment == 'off' => do nothing")
			return nil
		}
	}
}

func (r *LocalNewsAppReconciler) reconcileLocationExtractorService(ctx context.Context, app *kubdevv1alpha1.LocalNewsApp) error {
	log := ctrllog.FromContext(ctx)
	found := &v1.Service{}
	locationExtractorService := model.LocationExtractorService(app)
	err := r.Get(ctx, types.NamespacedName{Name: locationExtractorService.Name, Namespace: app.Namespace}, found)
	app.Status.ManagedResources[5] = "svc/" + locationExtractorService.Name
	if app.Spec.Extractor.Deployment == "" || app.Spec.Extractor.Deployment == "on" {
		if err != nil && errors.IsNotFound(err) {
			log.Info("Not Found: Creating")
			ctrl.SetControllerReference(app, locationExtractorService, r.Scheme)
			return r.Create(ctx, locationExtractorService)
		} else {
			log.Info("Found: Updating")
			reconciled := model.ReconcileLocationExtractorService(app, found)
			return r.Update(ctx, reconciled)
		}
	} else {
		if err == nil || !errors.IsNotFound(err) {
			log.Info("Found: But LocationExtractor service is set to 'off' => deleting")
			return r.Delete(ctx, locationExtractorService)
		} else {
			log.Info("Not found: But LocationExtractor service == 'off' => do nothing")
			return nil
		}
	}
}

func (r *LocalNewsAppReconciler) reconcileFeedScraperDeployment(ctx context.Context, app *kubdevv1alpha1.LocalNewsApp) error {
	log := ctrllog.FromContext(ctx)
	found := &v13.Deployment{}
	feedScraperDeployment := model.FeedScraperDeployment(app)
	err := r.Get(ctx, types.NamespacedName{Name: feedScraperDeployment.Name, Namespace: app.Namespace}, found)
	app.Status.ManagedResources[6] = "deployment/" + feedScraperDeployment.Name
	if app.Spec.FeedScraper.Deployment == "" || app.Spec.FeedScraper.Deployment == "on" {
		if err != nil && errors.IsNotFound(err) {
			log.Info("Not Found: Creating")
			ctrl.SetControllerReference(app, feedScraperDeployment, r.Scheme)
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

func (r *LocalNewsAppReconciler) reconcileFrontendDeployment(ctx context.Context, app *kubdevv1alpha1.LocalNewsApp) error {
	log := ctrllog.FromContext(ctx)
	found := &v13.Deployment{}
	frontendDeployment := model.FrontendDeployment(app)
	err := r.Get(ctx, types.NamespacedName{Name: frontendDeployment.Name, Namespace: app.Namespace}, found)
	app.Status.ManagedResources[7] = "deployment/" + frontendDeployment.Name
	if app.Spec.NewsFrontend.Deployment == "" || app.Spec.NewsFrontend.Deployment == "on" {
		if err != nil && errors.IsNotFound(err) {
			log.Info("Not Found: Creating")
			ctrl.SetControllerReference(app, frontendDeployment, r.Scheme)
			return r.Create(ctx, frontendDeployment)
		} else {
			log.Info("Found: Updating")
			reconciled := model.ReconcileFrontendDeployment(app, found)
			return r.Update(ctx, reconciled)
		}
	} else {
		if err == nil || !errors.IsNotFound(err) {
			log.Info("Found: But NewsFrontend deployment is set to 'off' => deleting")
			return r.Delete(ctx, frontendDeployment)
		} else {
			log.Info("Not found: But NewsFrontend deployment == 'off' => do nothing")
			return nil
		}
	}
}

func (r *LocalNewsAppReconciler) reconcileFrontendService(ctx context.Context, app *kubdevv1alpha1.LocalNewsApp) error {
	log := ctrllog.FromContext(ctx)
	found := &v1.Service{}
	frontendService := model.FrontendService(app)
	err := r.Get(ctx, types.NamespacedName{Name: frontendService.Name, Namespace: app.Namespace}, found)
	app.Status.ManagedResources[8] = "svc/" + frontendService.Name
	if app.Spec.NewsFrontend.Deployment == "" || app.Spec.NewsFrontend.Deployment == "on" {
		if err != nil && errors.IsNotFound(err) {
			log.Info("Not Found: Creating")
			ctrl.SetControllerReference(app, frontendService, r.Scheme)
			return r.Create(ctx, frontendService)
		} else {
			log.Info("Found: Updating")
			reconciled := model.ReconcileFrontendService(app, found)
			return r.Update(ctx, reconciled)
		}
	} else {
		if err == nil || !errors.IsNotFound(err) {
			log.Info("Found: But NewsFrontend service is set to 'off' => deleting")
			return r.Delete(ctx, frontendService)
		} else {
			log.Info("Not found: But NewsFrontend service == 'off' => do nothing")
			return nil
		}
	}
}

func (r *LocalNewsAppReconciler) reconcileFrontendIngress(ctx context.Context, app *kubdevv1alpha1.LocalNewsApp) error {
	log := ctrllog.FromContext(ctx)
	found := &networkingv1.Ingress{}
	frontendIngress := model.FrontendIngress(app)
	err := r.Get(ctx, types.NamespacedName{Name: frontendIngress.Name, Namespace: app.Namespace}, found)
	if app.Spec.NewsFrontend.Deployment == "" || app.Spec.NewsFrontend.Deployment == "on" {
		if err != nil && errors.IsNotFound(err) {
			log.Info("Not Found: Creating")
			ctrl.SetControllerReference(app, frontendIngress, r.Scheme)
			return r.Create(ctx, frontendIngress)
		} else {
			log.Info("Found: Updating")
			reconciled := model.ReconcileFrontendIngress(app, found)
			return r.Update(ctx, reconciled)
		}
	} else {
		if err == nil || !errors.IsNotFound(err) {
			log.Info("Found: But Frontend ingress is set to 'off' => deleting")
			return r.Delete(ctx, frontendIngress)
		} else {
			log.Info("Not found: But Frontend ingress == 'off' => do nothing")
			return nil
		}
	}
}

// SetupWithManager sets up the controller with the Manager.
func (r *LocalNewsAppReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&kubdevv1alpha1.LocalNewsApp{}).
		Owns(&v1.Service{}).
		Owns(&v13.Deployment{}).
		Owns(&networkingv1.Ingress{}).
		Complete(r)
}
