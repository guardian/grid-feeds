import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import type { App } from 'aws-cdk-lib';
import { InstanceClass, InstanceSize, InstanceType } from 'aws-cdk-lib/aws-ec2';
import { GuPlayWorkerApp } from '@guardian/cdk';
import { Fn } from 'aws-cdk-lib';
import { Effect, PolicyStatement } from 'aws-cdk-lib/aws-iam';
import { GuPolicy } from '@guardian/cdk/lib/constructs/iam';

export class AssociatedPressFeed extends GuStack {
	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);

		const gridIngestBucketArn = Fn.importValue(
			`S3WatcherIngestBucketARN-${props.stage === 'PROD' ? 'PROD' : 'TEST'}`,
		);

		const gridS3BucketPolicy = new PolicyStatement({
			effect: Effect.ALLOW,
			actions: ['s3:GetObject', 's3:PutObject', 's3:ListBucket'],
			resources: [`${gridIngestBucketArn}/ap/*`],
		});

		const instancePolicy = new GuPolicy(this, `${props.app}InstancePolicy`, {
			policyName: 'root',
			statements: [gridS3BucketPolicy],
		});

		new GuPlayWorkerApp(this, {
			app: props.app ?? 'associated-press-feed',
			instanceType: InstanceType.of(InstanceClass.T4G, InstanceSize.MICRO),
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
				additionalPolicies: [instancePolicy],
			},
			applicationLogging: {
				enabled: true,
			}
		});
	}
}
