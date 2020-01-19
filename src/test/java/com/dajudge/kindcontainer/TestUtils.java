package com.dajudge.kindcontainer;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.shaded.okhttp3.OkHttpClient;
import org.testcontainers.shaded.okhttp3.Request;
import org.testcontainers.shaded.okhttp3.Response;
import org.testcontainers.shaded.okhttp3.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;

import static java.util.UUID.randomUUID;

final class TestUtils {
    private static final Random RANDOM = new Random();

    private TestUtils() {
    }

    static String createNewNamespace(final KubernetesClient client) {
        final Namespace namespace = new NamespaceBuilder()
                .withNewMetadata()
                .withName(randomIdentifier())
                .endMetadata()
                .build();
        client.namespaces().create(namespace);
        return namespace.getMetadata().getName();
    }

    @NotNull
    static String randomIdentifier() {
        final String alphabet = "abcdefghijklmnopqrstuvwxyz";
        return alphabet.charAt(RANDOM.nextInt(alphabet.length()))
                + randomUUID().toString().replaceAll("-", "");
    }

    static Pod createSimplePod(final KubernetesClient client, final String namespace) {
        final Pod pod = new PodBuilder()
                .withNewMetadata()
                .withName(randomIdentifier())
                .withNamespace(namespace)
                .withLabels(new HashMap<String, String>() {{
                    put("app", "nginx");
                }})
                .endMetadata()
                .withNewSpec()
                .withContainers(new ContainerBuilder()
                        .withName("test")
                        .withImage("nginx")
                        .withPorts(new ContainerPortBuilder()
                                .withContainerPort(80)
                                .withProtocol("TCP")
                                .build())
                        .build())
                .endSpec()
                .build();
        client.pods().inNamespace(namespace).create(pod);
        return pod;
    }

    static Callable<Boolean> http(final String url) {
        return () -> {
            try {
                final OkHttpClient client = new OkHttpClient();
                final Request request = new Request.Builder().url(url).build();
                final Response response = client.newCall(request).execute();
                try (final ResponseBody body = response.body()) {
                    return response.code() == 200;
                }
            } catch (final IOException e) {
                return false;
            }
        };
    }

    static boolean isRunning(final KubernetesClient client, final HasMetadata pod) {
        return "Running".equals(client.pods()
                .inNamespace(pod.getMetadata().getNamespace())
                .withName(pod.getMetadata().getName())
                .get().getStatus().getPhase());
    }
}