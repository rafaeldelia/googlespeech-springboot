package com.arphoenix.googlespeech.service;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;

@Service
public class AuthService {

	/**
	 * Instancia de forma explícita o JSON da credencial e faz a conexão com o bucket no Google Cloud
	 * 
	 * Alterar o JSON para a sua credencial
	 * 
	 * @throws IOException
	 */
	public void auth() throws IOException {

		GoogleCredentials credentials = GoogleCredentials
				.fromStream(new FileInputStream("C:\\Users\\rsdelia\\Documents\\gcp-credentials.json"))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

		System.out.println("Buckets:");
		Page<Bucket> buckets = storage.list();
		for (Bucket bucket : buckets.iterateAll()) {
			System.out.println(bucket.toString());
		}
	}

	/**
	 * autenticar utilizando a variável de ambiente GOOGLE_APPLICATION_CREDENTIALS que deve apontar para o JSON da credencial gerada no
	 * Google Cloud Console
	 */
	public void authImplicit() {
		Storage storage = StorageOptions.getDefaultInstance().getService();
		System.out.println("Buckets:");
		Page<Bucket> buckets = storage.list();
		for (Bucket bucket : buckets.iterateAll()) {
			System.out.println(bucket.toString());
		}
	}
}