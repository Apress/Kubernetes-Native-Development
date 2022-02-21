package com.apress.kubdev.rss;

import static org.apache.camel.component.kubernetes.KubernetesConstants.KUBERNETES_JOB_NAME;
import static org.apache.camel.component.kubernetes.KubernetesConstants.KUBERNETES_JOB_SPEC;
import static org.apache.camel.component.kubernetes.KubernetesConstants.KUBERNETES_NAMESPACE_NAME;
import static org.apache.camel.component.kubernetes.KubernetesOperations.CREATE_JOB_OPERATION;

import java.io.InputStream;
import java.net.ConnectException;

import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class CrdRoute extends RouteBuilder {
	public enum Backend { DIRECT, JOB}
	@PropertyInject(value = "crd.analysis.controller.backend")
	Backend backend;
	private ObjectMapper mapper = new ObjectMapper();
	private static final Logger LOGGER = LoggerFactory.getLogger(CrdRoute.class);
	private KubernetesClient kubernetesClient;
	@Override
	public void configure() throws Exception {
		onException(ConnectException.class)
			.maximumRedeliveries("{{crd.analysis.controller.maximumRedeliveries:0}}")
			.useExponentialBackOff()
			.redeliveryDelay("{{crd.analysis.controller.redeliveryDelay:10000}}")
			.onRedelivery(p -> LOGGER.info("Redelivered"));
	
		kubernetesClient = new DefaultKubernetesClient();
		bindToRegistry("kubernetesClient", kubernetesClient);
		RouteDefinition route = from("kubernetes-custom-resources:///?kubernetesClient=#kubernetesClient&operation=createCustomResource&crdName=feedanalysis&crdGroup=kubdev.apress.com&crdScope=namespaced&crdVersion=v1&crdPlural=feedanalyses")
			.log("${body}");
		switch (backend) {
		case DIRECT:
			route.process(p -> {
				FeedAnalysis feedAnalysis = mapper.readValue(p.getIn().getBody().toString(), FeedAnalysis.class);
				p.getIn().setBody(mapper.writeValueAsString(feedAnalysis.getSpec().getUrls()));
			})
			.log("${body}")
			.to("rest:post:analysis?host={{crd.analysis.controller.scraperapi.host:localhost}}:{{crd.analysis.controller.scraperapi.port:9090}}");
			break;
		case JOB:
			route.process(p -> {
					FeedAnalysis feedAnalysis = mapper.readValue(p.getIn().getBody().toString(), FeedAnalysis.class);
					p.getIn().setHeader(KUBERNETES_NAMESPACE_NAME, feedAnalysis.getMetadata().getNamespace());
					p.getIn().setHeader(KUBERNETES_JOB_NAME, "camel-job");
					p.getIn().setHeader(KUBERNETES_JOB_SPEC, loadDeploymentManifest().getSpec());
				})
				.to("kubernetes-job:///?kubernetesClient=#kubernetesClient&operation=" + CREATE_JOB_OPERATION);
			break;	
		}
	}
	
	private Job loadDeploymentManifest() {
		InputStream manifest = CrdRoute.class.getClassLoader().getResourceAsStream("k8s/feed-scraper-job.yaml");
		return kubernetesClient.batch().v1().jobs().load(manifest).get();
	}
}
