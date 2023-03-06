# grid-feeds
This repo is intended as a monorepo for applications that read third-party feeds to fetch images that are consumed by the [grid](https://github.com/guardian/grid).

We are currently consuming one feed - Associated Press.

## Associated Press Feed
The AP Media API documentation can be found [here](http://api.ap.org/media/v/docs/index.html).

There is also some [Swagger API documentation](https://api.ap.org/media/v/swagger/)

We consume AP's media via their `/feed` endpoint, which uses [long polling](http://api.ap.org/media/v/docs/index.html#t=Feed.htm%23About_Long_Polling&rhsearch=long%20polling&rhsyns=%20) to reduce latency.

The app holds the requests open for a long time and the API will return a response when it next has images, or an empty response if no new images are available after 15 seconds. Once the app has received a response it will immediately call the next page to wait for further results.

### How to run
You will need media-service AWS credentials.

Run `scripts/fetch-config.sh` to fetch down DEV config.

Run `sbt associated-press-feed/run`

### Fixing CI failures
Our CI runs `scalafmtCheckAll` which checks our Scala is formatted correctly.

If this check fails, you can run `sbt scalafmtAll` in the project root fix formatting.

### Resetting the feed
We store the next page of the feed in DynamoDB which means we can pick up where we left off in the event of an outage or redeployment.

To reset the feed to start consuming from present time, delete the entry in the appropriate DynamoDB table.

The app will default back to the 'starter' url stored in parameter store.