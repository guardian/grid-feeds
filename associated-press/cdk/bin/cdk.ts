import { GuRoot } from '@guardian/cdk/lib/constructs/root';
import 'source-map-support/register';
import { AssociatedPressFeed } from '../lib/associated-press-feed';

const app = new GuRoot();

const env = { region: 'eu-west-1' };

new AssociatedPressFeed(app, 'AssociatedPressFeed-CODE', {
	stack: 'media-service',
	stage: 'CODE',
	app: 'associated-press-feed',
	env,
});

new AssociatedPressFeed(app, 'AssociatedPressFeed-PROD', {
	stack: 'media-service',
	stage: 'PROD',
	app: 'associated-press-feed',
	env,
});
