package com.academictracker.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Service for managing feedback using MongoDB (NoSQL)
 */
public class FeedbackService {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);
    
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> feedbackCollection;
    
    public FeedbackService() {
        initializeMongoConnection();
    }
    
    private void initializeMongoConnection() {
        try {
            Properties properties = new Properties();
            try (InputStream input = getClass().getClassLoader()
                    .getResourceAsStream("config/application.properties")) {
                if (input != null) {
                    properties.load(input);
                    String mongoUri = properties.getProperty("mongodb.uri");
                    String databaseName = properties.getProperty("mongodb.database");
                    
                    mongoClient = MongoClients.create(mongoUri);
                    database = mongoClient.getDatabase(databaseName);
                    feedbackCollection = database.getCollection("feedback");
                    
                    logger.info("MongoDB connection established");
                }
            }
        } catch (Exception e) {
            logger.error("Error connecting to MongoDB: " + e.getMessage(), e);
        }
    }
    
    /**
     * Submit feedback
     */
    public void submitFeedback(Long userId, String userRole, String feedbackType, 
                              String subject, String message) {
        try {
            Document feedback = new Document()
                .append("userId", userId)
                .append("userRole", userRole)
                .append("feedbackType", feedbackType)
                .append("subject", subject)
                .append("message", message)
                .append("timestamp", LocalDateTime.now().toString())
                .append("status", "PENDING");
            
            feedbackCollection.insertOne(feedback);
            logger.info("Feedback submitted by user: {}", userId);
        } catch (Exception e) {
            logger.error("Error submitting feedback: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all feedback
     */
    public List<Document> getAllFeedback() {
        List<Document> feedbackList = new ArrayList<>();
        try {
            feedbackCollection.find().into(feedbackList);
        } catch (Exception e) {
            logger.error("Error retrieving feedback: " + e.getMessage(), e);
        }
        return feedbackList;
    }
    
    /**
     * Get feedback by user ID
     */
    public List<Document> getFeedbackByUserId(Long userId) {
        List<Document> feedbackList = new ArrayList<>();
        try {
            feedbackCollection.find(new Document("userId", userId)).into(feedbackList);
        } catch (Exception e) {
            logger.error("Error retrieving user feedback: " + e.getMessage(), e);
        }
        return feedbackList;
    }
    
    /**
     * Update feedback status
     */
    public void updateFeedbackStatus(String feedbackId, String status) {
        try {
            feedbackCollection.updateOne(
                new Document("_id", feedbackId),
                new Document("$set", new Document("status", status))
            );
            logger.info("Feedback status updated: {}", feedbackId);
        } catch (Exception e) {
            logger.error("Error updating feedback status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Close MongoDB connection
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("MongoDB connection closed");
        }
    }
}
