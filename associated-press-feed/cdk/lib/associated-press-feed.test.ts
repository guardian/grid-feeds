import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { AssociatedPressFeed } from "./associated-press-feed";

describe("The AssociatedPressFeed stack", () => {
  it("matches the snapshot", () => {
    const app = new App();
    const stack = new AssociatedPressFeed(app, "AssociatedPressFeed", { stack: "media-service", stage: "TEST" });
    const template = Template.fromStack(stack);
    expect(template.toJSON()).toMatchSnapshot();
  });
});
