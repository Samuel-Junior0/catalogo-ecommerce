package br.edu.ifpb.catalogo_ecommerce.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirestoreConfig {

    @Bean
    public Firestore firestore(
            @Value("${firestore.project-id:demo-catalogo}") String projectId,
            @Value("${firestore.emulator-host:localhost:8080}") String emulatorHost) {

        // Não use FirestoreOptions.getDefaultInstance().toBuilder(): o build() tenta resolver as
        // Application Default Credentials e lança NullPointerException quando não há credencial
        // configurada. O projeto roda contra o emulador local, que não precisa de credenciais.
        return FirestoreOptions.newBuilder()
                .setProjectId(projectId)
                .setEmulatorHost(emulatorHost)
                .build()
                .getService();
    }
}
