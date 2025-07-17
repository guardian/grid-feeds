import { GuPlayWorkerApp } from '@guardian/cdk';
import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import { GuAllowPolicy } from '@guardian/cdk/lib/constructs/iam';
import type { App } from 'aws-cdk-lib';
import { Fn, Tags } from 'aws-cdk-lib';
import { AttributeType, BillingMode, Table } from 'aws-cdk-lib/aws-dynamodb';
import {
	InstanceClass,
	InstanceSize,
	InstanceType,
	SecurityGroup,
} from 'aws-cdk-lib/aws-ec2';
import { Bucket } from 'aws-cdk-lib/aws-s3';
import { StringParameter } from 'aws-cdk-lib/aws-ssm';

export class AssociatedPressFeed extends GuStack {
	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);

		const gridIngestBucketArn = Fn.importValue(
			`IngestQueueBucketArn-${props.stage === 'PROD' ? 'PROD' : 'TEST'}`,
		);

		const gridIngestBucket = Bucket.fromBucketArn(
			this,
			'gridIngestBucket',
			gridIngestBucketArn,
		);

		const { stage, stack, app } = props;
		new StringParameter(this, 'GridIngestBucketName', {
			parameterName: `/${stage}/${stack}/${app}/aws/s3/uploadBucketName`,
			stringValue: gridIngestBucket.bucketName,
			description:
				'The s3 bucket name to upload images to. [AUTOMATICALLY UPDATED FROM CDK/CFN]',
		});

		const nextPageTable = new Table(this, 'associatedPressFeedNextPageTable', {
			partitionKey: { name: 'key', type: AttributeType.STRING },
			tableName: `${props.app ?? 'associated-press-feed'}-${props.stage}`,
			billingMode: BillingMode.PAY_PER_REQUEST,
		});

		// Enable automated backups via https://github.com/guardian/aws-backup
		Tags.of(nextPageTable).add('devx-backup-enabled', 'true');

		const additionalPolicies = [
			new GuAllowPolicy(this, 's3GridIngestBucket', {
				resources: [`${gridIngestBucketArn}/ap/*`],
				actions: ['s3:PutObject'],
			}),
			new GuAllowPolicy(this, 'nextPageTable', {
				resources: [nextPageTable.tableArn],
				actions: ['dynamodb:*'],
			}),
		];

		const ec2App = new GuPlayWorkerApp(this, {
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
					fileName: 'associated-press-feed.deb',
					executionStatement:
						'dpkg -i /associated-press-feed/associated-press-feed.deb',
				},
			},
			roleConfiguration: {
				additionalPolicies,
			},
			applicationLogging: {
				enabled: true,
			},
			imageRecipe: {
				Recipe: 'editorial-tools-jammy-java17',
			},
			instanceMetricGranularity: stage === 'PROD' ? '1Minute' : '5Minute',
		});

		const { vpc } = ec2App;

		// A temporary security group with a fixed logical ID, replicating the one removed from GuCDK v61.5.0.
		const tempSecurityGroup = new SecurityGroup(this, 'WazuhSecurityGroup', {
			vpc,
			// Must keep the same description, else CloudFormation will try to replace the security group
			// See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-ec2-securitygroup.html#cfn-ec2-securitygroup-groupdescription.
			description: 'Allow outbound traffic from wazuh agent to manager',
		});
		this.overrideLogicalId(tempSecurityGroup, {
			logicalId: 'WazuhSecurityGroup',
			reason:
				"Part one of updating to GuCDK 61.5.0+ whilst using Riff-Raff's ASG deployment type",
		});
	}
}
