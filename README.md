# Avenue 

-----------------------------
![Status](https://img.shields.io/badge/Status-Experimental-important)
[![Build Status](https://travis-ci.org/Seputaes/avenue.svg?branch=master)](https://travis-ci.org/Seputaes/avenue)
[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://seputaes.mit-license.org/)
[![made-with-java](https://img.shields.io/badge/Made%20with-Java-1f425f.svg)](https://en.wikipedia.org/wiki/Java_%28programming_language%29)
[![GitHub issues](https://img.shields.io/github/issues/Seputaes/avenue.svg)](https://GitHub.com/seputaes/avenue/issues/)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat)](http://makeapullrequest.com)
[![Discord](https://img.shields.io/discord/481539443170344961?logo=discord&label=Discord)](https://sep.gg/discord)

Avenue is a simple and straightforward annotation-style HTTP router 
for building serverless HTTP applications on [AWS Lambda](https://aws.amazon.com/lambda/) + [AWS API Gateway](https://aws.amazon.com/api-gateway/).

It is designed to be a "catch all" router, where the routing is done on 
the application side rather than at the API Gateway layer. It is designed to support
customizable path "token" parsing with a flavor similar to that of the Werkzeug/Flask frameworks.

The scope of Avenue is intentionally left small. While other frameworks provide
similar capabilities and dozens of powerful features (which may never be used), 
Avenue sets out to give the developer full control over their own implementation 
while simply providing a layer between AWS Lambda and your application which takes care of the routing.

Avenue is tested to be working for both AWS Lambda's JDK8 and JDK11 runtimes.

## Basic Usage

### Quick Example

#### Step 1: Define Some Routes

##### MyRoutes.java
```java
public class MyRoutes extends AbstractRouteController {
    @GET("/hello/<string:name>")
    public AwsProxyResponse home(@Path("name") final String name) {
        return AwsResponseBuilder.newBuilder()
            .html()
            .stringBody("Hello, " + name + "!")
            .build();
    }
}
```

#### Step 2: Create a Handler class and add your routes
##### MyLambdaHandler.java
```java
public class MyLambdaHandler extends BasicLambdaProxyHandler {
    public MyLambdaHander() {
        super();
        registerController(new MyRoutes());
    }
}
```

##### Step 3: Set your parameterEvaluator class as the Lambda's parameterEvaluator:
Eg: `path.to.MyHandler::handleRequest`


## Contributing
Avenue is in its early stages of development. It functions adequately for the most basic
of routing tasks, but is slowly developing into something more fully featured.