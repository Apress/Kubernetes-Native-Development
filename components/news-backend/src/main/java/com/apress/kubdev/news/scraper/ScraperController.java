package com.apress.kubdev.news.scraper;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.quarkus.runtime.StartupEvent;

@Path("/scraper")
public class ScraperController {
	private static final String DEPLOYMENT_NAME_PREFIX = "scraper-";
	private static final Logger LOGGER = LoggerFactory.getLogger(ScraperController.class);
	
	@ConfigProperty(name = "backend.crd.enable", defaultValue = "false")
	private boolean enableCrdWatch;
	
	@Inject
	private KubernetesClient kubernetesClient;

	void watchOnCrd(@Observes StartupEvent event) { 
		if (enableCrdWatch) {
			try {
				String namespace = kubernetesClient.getNamespace();
				kubernetesClient.customResources(FeedAnalysis.class).inNamespace(namespace).watch(new Watcher<FeedAnalysis>() {
		
					@Override
					public void eventReceived(Action action, FeedAnalysis resource) {
						if (Action.ADDED.equals(action)) {
							LOGGER.info("New feed analysis created");
							createDeployment(resource.getSpec().getUrls());
						}
					}
		
					@Override
					public void onClose(WatcherException cause) {
						LOGGER.error("Watcher stopped", cause);
					}		
				});
			} catch (Exception e) {
				LOGGER.error("Error watching feed analysis crd", e);
			}
		}
	}
	
	@GET
    @Path("/")
    public DeploymentList list() {
		String namespace = kubernetesClient.getNamespace();
		return kubernetesClient.apps()
			.deployments()
			.inNamespace(namespace)
			.withLabelIn("app", "news-backend")
			.list();
    }
	
	@POST
    @Path("/")
    public Deployment scrape(@RequestBody List<String> urls) {
		return createDeployment(urls);
    }

	private Deployment createDeployment(List<String> urls) {
		String namespace = kubernetesClient.getNamespace();
		LOGGER.debug("Creating deployment with urls {} in namespace {}", urls, namespace);
		Deployment deployment = loadDeploymentManifest();
		String deploymentName = generateDeploymentName(namespace);
		LOGGER.debug("Deployment name will be {}", deploymentName);
		deployment.getMetadata().setName(deploymentName);
		deployment.getMetadata().setNamespace(namespace);
		setFeedsUrlEnvVar(urls, deployment);
		return kubernetesClient.apps()
				.deployments()
				.inNamespace(namespace)
				.createOrReplace(deployment);
	}

	private String generateDeploymentName(String namespace) {
		String deploymentName = DEPLOYMENT_NAME_PREFIX + (countDeployments(namespace) + 1);
		return deploymentName;
	}

	private void setFeedsUrlEnvVar(List<String> urls, Deployment deployment) {
		deployment.getSpec()
				.getTemplate()
				.getSpec()
				.getContainers()
		        .stream()
				.filter(c -> "feed-scraper".equals(c.getName()))
				.findFirst()
				.ifPresent(c -> c.getEnv()
						.add(new EnvVarBuilder()
								.withName("SCRAPER_FEEDS_URL")
								.withValue(urls.stream().collect(Collectors.joining(",")))
								.build()));
	}

	private Deployment loadDeploymentManifest() {
		InputStream manifest = ScraperController.class.getClassLoader().getResourceAsStream("k8s/feed-scraper-deployment.yaml");
		return kubernetesClient.apps().deployments().load(manifest).get();
	}
	
	private long countDeployments(String namespace) {
		return kubernetesClient.apps().deployments().inNamespace(namespace).list().getItems().stream()
				.filter(i -> i.getMetadata().getName().startsWith(DEPLOYMENT_NAME_PREFIX))
				.count();
		
	}
	
}
