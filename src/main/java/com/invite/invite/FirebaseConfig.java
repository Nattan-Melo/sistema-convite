package com.invite.invite;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {
    
    @Value("${firebase.rtdb.url}")
    private String servUrl;
    
    @Value("${firebase.rtdb.acc-id}")
    private String accId;

    @Value("${firebase.rtdb.service-acc-key}")
    private String servAccId;

    @Bean
    FirebaseApp firebaseApp() throws IOException 
    {
        ClassPathResource resource = new ClassPathResource(servAccId);
        InputStream stream = resource.getInputStream();
        FirebaseOptions opt = FirebaseOptions.builder()
                .setServiceAccountId(accId)
				.setCredentials(GoogleCredentials.fromStream(stream))
				.setDatabaseUrl(servUrl)
				.build();

		if(FirebaseApp.getApps().isEmpty())
		{
			return FirebaseApp.initializeApp(opt);
		}
        return FirebaseApp.getInstance();
    }
}
