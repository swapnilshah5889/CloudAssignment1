package com.pa1.textdetectionapp.textdetectionapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootApplication
public class TextDetectionApp {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(TextDetectionApp.class, args);

		// Init S3
		S3Service s3Service = new S3Service();
		SQSService sqsService = new SQSService();
		SqsClient sqsClient = sqsService.getSqsClient();
		String queueUrl = sqsService.getSQSQueueUrl(sqsClient);

		// Init Text detection service
		TextDetectionService textDetectionService = new TextDetectionService();
		RekognitionClient rekognitionClient = textDetectionService.getRekognitionClient();

		log.info("queueUrl: {}", queueUrl);
		Map<String, String> mp = new HashMap<>();

		// Infinite loop
		while(true) {
			// Fetch new messages from the queue
			Message message = sqsService.pollQueueMessage(sqsClient, queueUrl);

			// Sleep thread if no new message available
			if(message==null) {
				Thread.sleep(1000);
				continue;
			}

			// Delete Message Request
			DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
					.queueUrl(queueUrl)
					.receiptHandle(message.receiptHandle())
					.build();
			sqsClient.deleteMessage(deleteMessageRequest);

			// Exit loop if message is -1
			if(message.body().equals("-1")) {
				break;
			}
			// Process the image
			else {
				Image image = s3Service.s3FetchByName(message.body());
				// Detect text from the image
				textDetectionService.detectTextFromImage(rekognitionClient, image, message.body(), mp);
			}
		}

		// Write result to file
		FileWriteService fileWriteService = new FileWriteService();
		fileWriteService.fileWrite(mp);
	}

}
