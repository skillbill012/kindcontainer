package com.dajudge.kindcontainer;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import org.junit.Test;

import java.util.HashMap;

import static com.dajudge.kindcontainer.TestUtils.*;
import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;

public class NodePortTest extends BaseKindContainerTest {
    @Test
    public void exposes_node_port() {
        final Pod pod = K8S.withClient(client -> {
            return createSimplePod(client, namespace);
        });
        K8S.withClient(client -> {
            client.services().create(new ServiceBuilder()
                    .withNewMetadata()
                    .withName("nginx")
                    .withNamespace(pod.getMetadata().getNamespace())
                    .endMetadata()
                    .withNewSpec()
                    .withType("NodePort")
                    .withSelector(new HashMap<String, String>() {{
                        put("app", "nginx");
                    }})
                    .withPorts(new ServicePortBuilder()
                            .withNodePort(30000)
                            .withPort(80)
                            .withTargetPort(new IntOrString(80))
                            .withProtocol("TCP")
                            .build())
                    .endSpec()
                    .build());
            await("testpod")
                    .timeout(ofSeconds(300))
                    .until(TestUtils.http("http://localhost:" + K8S.getMappedPort(30000)));

        });
    }

}
