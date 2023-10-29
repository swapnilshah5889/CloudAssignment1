package com.pa1.carrecognitionapp.service;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.List;

@SpringBootApplication
@Slf4j
public class CarRecognitionAppApplication {

	private static String bucketName = "njit-cs-643";

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(CarRecognitionAppApplication.class, args);

		// Init S3
		S3Service s3Service = new S3Service();
		S3Client s3Client = s3Service.getS3Client();
		List<S3Object> images = s3Service.fetchS3Objects(s3Client);

		// Init SQS
		SQSService sqsService = new SQSService();
		SqsClient sqsClient = sqsService.getSqsClient();
		String queueUrl = sqsService.getQueueUrl(sqsClient);

		// Build Rekognition Objects
		RekognitionService rekognitionService = new RekognitionService();
		RekognitionClient rekognitionClient = rekognitionService.getRekognitionClient();

		log.info("queueUrl: {}", queueUrl);
		// Iterate over all images
		for(S3Object img : images) {
			// If image has car label
			if (rekognitionService.recognizeImage(rekognitionClient, img, bucketName)) {
				log.info("Image with car label found, Confidence: {}", img.key());
				// SQS push successful
				if(sqsService.pushSQSMessage(sqsClient, img.key(), queueUrl))
					log.info("Message push successful: {}", img.key());
				else
					log.info("Error while pushing the message: {}", img.key());
			}
			// Image does not have car label
			else {
				log.info("The image does not have car label: {}", img.key());
			}

		}

		sqsService.pushSQSMessage(sqsClient, "-1", queueUrl);
		log.info("End of queue: -1");
	}
}

