package br.edu.ifpb.catalogo_ecommerce.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirestoreConfig {

    @Bean
    public Firestore firestore() {
        return FirestoreOptions.getDefaultInstance().toBuilder()
                .setProjectId("demo-catalogo")
                .build()
                .getService();
    }
}