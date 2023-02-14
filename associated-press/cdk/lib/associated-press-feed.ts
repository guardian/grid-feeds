import { GuPlayApp } from '@guardian/cdk';
import { AccessScope } from '@guardian/cdk/lib/constants';
import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import type { App } from 'aws-cdk-lib';
import { InstanceClass, InstanceSize, InstanceType } from 'aws-cdk-lib/aws-ec2';

export class AssociatedPressFeed extends GuStack {
	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);
		new GuPlayApp(this, {
			app: 'associated-press-feed',
			access: { scope: AccessScope.INTERNAL, cidrRanges: [] },
			instanceType: InstanceType.of(InstanceClass.T4G, InstanceSize.MICRO),
			monitoringConfiguration: { noMonitoring: true },
			certificateProps: {
				domainName: 'associated-press-feed.code.dev-gutools.co.uk',
			},
			scaling: { minimumInstances: 1 },
			userData: {
				distributable: {
					fileName: `associated-press-feed.deb`,
					executionStatement: `dpkg -i /associated-press-feed/associated-press-feed.deb`,
				},
			},
		});
	}
}
