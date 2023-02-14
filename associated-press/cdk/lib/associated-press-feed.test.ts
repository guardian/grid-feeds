import {App} from 'aws-cdk-lib';
import {Template} from 'aws-cdk-lib/assertions';
import {AssociatedPressFeed} from './associated-press-feed';
import {InstanceSize} from "aws-cdk-lib/aws-ec2";

describe('The AssociatedPressFeed stack', () => {
	it('matches the snapshot', () => {
		const app = new App();
		const stack = new AssociatedPressFeed(app, 'AssociatedPressFeed', {
			stack: 'media-service',
			stage: 'TEST',
			instanceSize: InstanceSize.NANO,
			domainName: "test"
		});
		const template = Template.fromStack(stack);
		expect(template.toJSON()).toMatchSnapshot();
	});
});
