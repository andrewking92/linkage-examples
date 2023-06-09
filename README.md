# linkage-examples

# JAR
java -Dmongodb.uri="mongodb://localhost:27017" -Dmongodb.database="test" -Dmongodb.collection="Party" -jar target/linkage-examples-1.0-SNAPSHOT-jar-with-dependencies.jar

java -Dmongodb.uri="mongodb://localhost:27017" -Dmongodb.database="test" -Dmongodb.collection="PartySite" -jar target/linkage-examples-1.0-SNAPSHOT-jar-with-dependencies.jar

# Linkage - Party
```
db.Party.aggregate([
    {
        $match: {
            partyID: { $in: partyIDs }
        }
    },
    {
        $lookup: {
            from: "Linkage",
            localField: "partyID",
            foreignField: "party.partyID",
            as: "linkage"
        }
    },
    {
        $addFields: {
            accountKeys: {
                $reduce: {
                    input: "$linkage",
                    initialValue: [],
                    in: {
                        $setUnion: ["$$value", ["$$this.account.accountKey"]]
                    }
                }
            }
        }
    },
    {
        $project: {
            linkage: 0
        }
    }
])
```


# Linkage - PartySite
```
db.PartySite.aggregate([
    {
        $match: {
            siteID: { $in: siteIDs }
        }
    },
    {
        $lookup: {
            from: "PSALinkage",
            localField: "siteID",
            foreignField: "partySite.siteID",
            as: "linkage"
        }
    },
    {
        $addFields: {
            accountKeys: {
                $reduce: {
                    input: "$linkage",
                    initialValue: [],
                    in: {
                        $setUnion: ["$$value", ["$$this.account.accountKey"]]
                    }
                }
            }
        }
    },
    {
        $project: {
            linkage: 0
        }
    }
])
```


# Linkage - Account
```
db.Account.aggregate([
    {
        $match: {
            accountKey: { $in: accountKeys }
        }
    },
    {
        $lookup: {
            from: "Linkage",
            localField: "accountKey",
            foreignField: "account.accountKey",
            as: "linkage"
        }
    },
    {
        $addFields: {
            accountPartyRoleList: {

                $reduce: {
                    input: "$linkage",
                    initialValue: [],
                    in: {
                        $setUnion: ["$$value", ["$$this.account.accountKey"]]
                    }
                }
            }
        }
    },
    {
        $project: {
            linkage: 0
        }
    }
])
```
