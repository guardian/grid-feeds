import { GuPlayWorkerApp } from '@guardian/cdk';
import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import { GuAllowPolicy } from '@guardian/cdk/lib/constructs/iam';
import type { App } from 'aws-cdk-lib';
import { Fn, Tags } from 'aws-cdk-lib';
import { AttributeType, Table } from 'aws-cdk-lib/aws-dynamodb';
import { InstanceClass, InstanceSize, InstanceType } from 'aws-cdk-lib/aws-ec2';

export class AssociatedPressFeed extends GuStack {
	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);

		const gridIngestBucketArn = Fn.importValue(
			`S3WatcherIngestBucketARN-${props.stage === 'PROD' ? 'PROD' : 'TEST'}`,
		);

		const nextPageTable = new Table(this, 'associatedPressFeedNextPageTable', {
			partitionKey: { name: 'key', type: AttributeType.STRING },
			tableName: `${props.app ?? 'associated-press-feed'}-${props.stage}`,
			readCapacity: 50,
			writeCapacity: 50
		});

		// Enable automated backups via https://github.com/guardian/aws-backup
		Tags.of(nextPageTable).add("devx-backup-enabled", "true");

		new GuPlayWorkerApp(this, {
			app: props.app ?? 'associated-press-feed',
			instanceType: InstanceType.of(InstanceClass.T4G, InstanceSize.SMALL),
			monitoringConfiguration: {
				snsTopicName: 'pagerduty-notification-topic',
				http5xxAlarm: {
					tolerated5xxPercentage: 5,
				},
				unhealthyInstancesAlarm: true,
			},
			scaling: { minimumInstances: 1, maximumInstances: 2 },
			userData: {
				distributable: {
					fileName: `associated-press-feed.deb`,
					executionStatement: `dpkg -i /associated-press-feed/associated-press-feed.deb`,
				},
			},
			roleConfiguration: {
				additionalPolicies: [
					new GuAllowPolicy(this, 's3GridIngestBucket', {
						resources: [`${gridIngestBucketArn}/ap/*`],
						actions: ['s3:PutObject'],
					}),
					new GuAllowPolicy(this, 'nextPageTable', {
						resources: [nextPageTable.tableArn],
						actions: ['dynamodb:*'],
					}),
				],
			},
			applicationLogging: {
				enabled: true,
			},
		});
	}
}
