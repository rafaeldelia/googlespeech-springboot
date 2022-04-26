package com.arphoenix.googlespeech.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.arphoenix.googlespeech.util.Message;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.ReadChannel;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
import com.google.cloud.storage.Storage;
import com.google.protobuf.ByteString;

@Service
public class GoogleCloudService {

	@Autowired
	private AuthService authService;

	@Autowired
	private Storage storage;

	/**
	 * autenticar com a credencial
	 * @throws IOException
	 */
	private void autenticarGoogleCloud() throws IOException {
		authService.auth();
	}

	/**
	 * Transcrever audio a partir de um path+filename
	 * @param pathFilename
	 * @return
	 * @throws Exception
	 */
	public Message transcribeFromLocalFile(String pathFilename) throws Exception {

		autenticarGoogleCloud();

		Message message = new Message();

		try (SpeechClient speech = SpeechClient.create()) {

			Path path = Paths.get(pathFilename);

			byte[] data = Files.readAllBytes(path);

			ByteString audioBytes = ByteString.copyFrom(data);

			String languageCode = "pt-BR";

			RecognitionConfig.Builder builder = RecognitionConfig.newBuilder().setEncoding(RecognitionConfig.AudioEncoding.FLAC)
					.setLanguageCode(languageCode);

			builder.setModel("default");

			RecognitionConfig config = builder.build();

			RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

			// Use non-blocking call for getting file transcription
			OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response = speech.longRunningRecognizeAsync(config,
					audio);

			while (!response.isDone()) {
				System.out.println("Waiting for response...");
				Thread.sleep(10000);
			}

			message = popularMessage(response);
		}

		return message;
	}

	/**
	 * transcrever a partir de uma URI criada no bucket do Google
	 * @param gcsUri
	 * @return
	 * @throws Exception
	 */
	public Message transcribeFromURI(String gcsUri) throws Exception {

		autenticarGoogleCloud();

		Message message = null;

		try (SpeechClient speech = SpeechClient.create()) {

			String languageCode = "pt-BR";

			RecognitionConfig.Builder builder = RecognitionConfig.newBuilder().setEncoding(AudioEncoding.FLAC).setLanguageCode(languageCode)
					.setEnableAutomaticPunctuation(true).setEnableWordTimeOffsets(true).setEnableWordConfidence(true)
					.setEnableSeparateRecognitionPerChannel(true);

			builder.setModel("default");

			RecognitionConfig config = builder.build();

			RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

			// Use non-blocking call for getting file transcription
			OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response = speech.longRunningRecognizeAsync(config,
					audio);

			while (!response.isDone()) {
				System.out.println("Waiting for response...");
				Thread.sleep(10000);
			}

			message = popularMessage(response);
		}

		return message;
	}

	/**
	 * Transcrever da storage da conta do Google
	 * @return
	 * @throws IOException
	 */
	public Message transcribeFromStorage() throws IOException {
		StringBuilder sb = new StringBuilder();

		try (ReadChannel channel = storage.reader("conta-delia", "config.txt")) {
			ByteBuffer bytes = ByteBuffer.allocate(64 * 1024);
			while (channel.read(bytes) > 0) {
				bytes.flip();
				String data = new String(bytes.array(), 0, bytes.limit());
				sb.append(data);
				bytes.clear();
			}
		}

		Message message = new Message();
		message.setContents(sb.toString());

		return message;
	}

	/**
	 * @param message
	 * @param response
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private static Message popularMessage(OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response)
			throws InterruptedException, ExecutionException {

		Message message = new Message();

		List<SpeechRecognitionResult> results = response.get().getResultsList();

		StringBuilder transcription = new StringBuilder();
		for (SpeechRecognitionResult result : results) {
			SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
			transcription.append(alternative.getTranscript());
		}
		message.setContents(transcription.toString());

		return message;
	}
}