# grid-feeds
Reads agency APIs and writes them to a location to be consumed into the grid

## Associated Press Feed

### How to run
Run `sbt associated-press-feed/run` from the root of the project.

### Fixing CI failures
Our CI runs `scalafmtCheckAll` which checks our Scala is formatted correctly.

If this check fails, you can run `sbt scalafmtAll` in the project root fix formatting.