package com.linkage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.MongoClientSettings;
import com.mongodb.ConnectionString;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import org.bson.conversions.Bson;


public class Linkage {
    private static final String MONGODB_URI = System.getProperty("mongodb.uri");
    private static final String DATABASE_NAME = System.getProperty("mongodb.database");
    private static final String COLLECTION_NAME = System.getProperty("mongodb.collection");

    public static void main( String[] args ) {

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(MONGODB_URI))
                .serverApi(serverApi)
                .build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            // Get the database and collection
            MongoCollection<Document> collection = mongoClient.getDatabase(DATABASE_NAME)
                    .getCollection(COLLECTION_NAME);

            List<String> partyIDs = Arrays.asList("party1", "party2"); // List of partyIDs

            List<Bson> pipeline = new ArrayList<>();

            // Match stage
            pipeline.add(Aggregates.match(Filters.in("partyID", partyIDs)));

            // Lookup stage
            pipeline.add(Aggregates.lookup("Linkage", "partyID", "party.partyID", "linkage"));

            // AddFields stage
            Document addFieldsStage = new Document("$addFields", new Document("accountKeys",
                    new Document("$reduce", new Document("input", "$linkage")
                            .append("initialValue", new ArrayList<>())
                            .append("in", new Document("$setUnion", Arrays.asList("$$value", Arrays.asList("$$this.account.accountKey")))))));
            pipeline.add(addFieldsStage);

            // Project stage
            Document projectStage = new Document("$project", new Document("linkage", 0));
            pipeline.add(projectStage);

            // Execute aggregation
            MongoCursor<Document> cursor = collection.aggregate(pipeline).iterator();

            // Process the results
            while (cursor.hasNext()) {
                Document result = cursor.next();
                // Process the document as needed
                System.out.println(result.toJson());
            }

            cursor.close();
        }
    }
}