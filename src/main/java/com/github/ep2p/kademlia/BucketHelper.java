package com.github.ep2p.kademlia;

public class BucketHelper {

    public static void addToAnswer(Bucket bucket, FindNodeAnswer answer, int destination) {
        for (int id : bucket.getNodes()) {
            answer.getNodes().add(new Contact(id,id ^ destination));
        }
    }

}
