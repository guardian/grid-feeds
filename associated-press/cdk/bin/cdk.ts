import 'source-map-support/register';
import { App } from 'aws-cdk-lib';
import { AssociatedPressFeed } from '../lib/associated-press-feed';

const app = new App();
new AssociatedPressFeed(app, 'AssociatedPressFeed-CODE', {
	stack: 'media-service',
	stage: 'CODE',
});
