import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import type { App } from 'aws-cdk-lib';
import { InstanceClass, InstanceSize, InstanceType } from 'aws-cdk-lib/aws-ec2';
import { GuPlayWorkerApp } from "@guardian/cdk";

export class AssociatedPressFeed extends GuStack {
	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);
		new GuPlayWorkerApp(this, {
			app: 'associated-press-feed',
			instanceType: InstanceType.of(InstanceClass.T4G, InstanceSize.MICRO),
			monitoringConfiguration: {
				snsTopicName: "pagerduty-notification-topic",
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
		});
	}
}
