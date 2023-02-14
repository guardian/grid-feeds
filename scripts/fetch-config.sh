#!/usr/bin/env bash

aws s3 cp s3://grid-conf/DEV/associated-press-feed.private.conf ~/.gu/associated-press-feed.private.conf --profile media-service --region eu-west-1
