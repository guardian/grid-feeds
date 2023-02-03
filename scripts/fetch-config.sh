#!/usr/bin/env bash

aws s3 cp s3://grid-conf/DEV/grid-feeds.private.conf /etc/gu/grid-feeds.private.conf --profile media-service --region eu-west-1
