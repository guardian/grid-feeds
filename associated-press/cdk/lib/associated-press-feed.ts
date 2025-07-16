import { GuPlayWorkerApp } from '@guardian/cdk';
import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack, GuStringParameter } from '@guardian/cdk/lib/constructs/core';
import { GuVpc } from '@guardian/cdk/lib/constructs/ec2';
import { GuAllowPolicy } from '@guardian/cdk/lib/constructs/iam';
import type { App } from 'aws-cdk-lib';
import { Fn, Tags } from 'aws-cdk-lib';
import { AttributeType, BillingMode, Table } from 'aws-cdk-lib/aws-dynamodb';
import { InstanceClass, InstanceSize, InstanceType } from 'aws-cdk-lib/aws-ec2';
import { Bucket } from 'aws-cdk-lib/aws-s3';
import { StringParameter } from 'aws-cdk-lib/aws-ssm';

type AssociatedPressFeedProps = GuStackProps & {
	app: string;
};

export class AssociatedPressFeed extends GuStack {
	constructor(scope: App, id: string, props: AssociatedPressFeedProps) {
		super(scope, id, props);

		const { stage, stack, app } = props;

		const gridIngestBucketArn = Fn.importValue(
			`IngestQueueBucketArn-${props.stage === 'PROD' ? 'PROD' : 'TEST'}`,
		);

		const gridIngestBucket = Bucket.fromBucketArn(
			this,
			'gridIngestBucket',
			gridIngestBucketArn,
		);

		new StringParameter(this, 'GridIngestBucketName', {
			parameterName: `/${stage}/${stack}/${app}/aws/s3/uploadBucketName`,
			stringValue: gridIngestBucket.bucketName,
			description:
				'The s3 bucket name to upload images to. [AUTOMATICALLY UPDATED FROM CDK/CFN]',
		});

		const nextPageTable = new Table(this, 'associatedPressFeedNextPageTable', {
			partitionKey: { name: 'key', type: AttributeType.STRING },
			tableName: `${app}-${stage}`,
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

		new GuPlayWorkerApp(this, {
			app,
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
					executionStatement: `dpkg -i /${app}/associated-press-feed.deb`,
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

		/**
		 * The `VpcId` in Cloudformation for this stack has the same _default_ value as the param
		 * we're creating here, but its actual value has been overwritten for use by the existing
		 * GuPlayWorkerApp. So we're temporarily creating a new param that can hold the correct
		 * value for the new app.
		 */
		const vpcIdParam = new GuStringParameter(this, 'PrimaryVpcId', {
			fromSSM: true,
			default: '/account/vpc/primary/id',
		});

		const vpc = GuVpc.fromId(this, 'ApplicationVpc', {
			vpcId: vpcIdParam.valueAsString,
		});

		const v2AppName = `${app}-v2`;

		new GuPlayWorkerApp(this, {
			app: v2AppName,
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
					executionStatement: `dpkg -i /${v2AppName}/associated-press-feed.deb`,
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
			vpc,
		});
	}
}
