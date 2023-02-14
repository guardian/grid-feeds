import 'source-map-support/register';
import { App } from 'aws-cdk-lib';
import { AssociatedPressFeed } from '../lib/associated-press-feed';
import { InstanceSize } from 'aws-cdk-lib/aws-ec2';

const app = new App();
new AssociatedPressFeed(app, 'AssociatedPressFeed-CODE', {
	stack: 'media-service',
	stage: 'CODE',
	instanceSize: InstanceSize.MICRO,
	domainName: 'code-guardian.com'
});
