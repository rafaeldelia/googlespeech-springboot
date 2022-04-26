package com.arphoenix.googlespeech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.arphoenix.googlespeech.service.GoogleCloudService;
import com.arphoenix.googlespeech.util.Message;

@RestController
@RequestMapping("/speech")
public class GCUriController {

	@Autowired
	private GoogleCloudService googleCloudService;

	@GetMapping(path = "/fromLocalFile")
	public Message transcribeFromLocalFile() throws Exception {
		return googleCloudService.transcribeFromLocalFile("C:\\maria.flac");

	}

	@GetMapping(path = "/fromURI")
	public Message transcribeFromURI() throws Exception {
		return googleCloudService.transcribeFromURI("gs://bucket_conta_delia/maria.flac");

	}

	@RequestMapping(path = { "/fromStorage" }, method = { RequestMethod.GET })
	public Message transcribeFromStorage() throws Exception {
		return googleCloudService.transcribeFromStorage();
	}
}