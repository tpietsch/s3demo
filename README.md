# Intro

Things could be cleaned up but took a few liberties in place of cleanliness to potentially explore during live session. 

## Reasoning
- Why you chose to implement what you did
Spring framework offers various enterprise focused features out of box particularly SpringSecurity for roles/perms (its been around for some time)
Netty server is reasonably performant https://www.techempower.com/benchmarks/#section=data-r23&test=json 
Possibly more to gain form closer to machine java servers - vertex

Was looking to showcase leveraging something out of box that I am familiar with that handles roles/perms well. 
This sample doesnt even scratch the use case of complexity support

This particular project is using webflux + reactor. I might consider it a bit advanced even for a java dev but the way it interacts with network IO
is using non blocking IO - so reactor forces you to program functionally and the way data is passed around is very event driven instead of leveraging thread pools.

I dont think its the easiest framework to understand so in a realistic scenario I dont think i would choose depending on team. 
Possible folks who worked with Scala would find it more natural than others.

- Considerations, decisions, assumptions you made
Nothing too deep mostly trying to be expressive to demo. Decisions were more so due to getting something out rather than having the best solution.
In a more realistic scenario what should very much be considered is team support/familiarity. Scaling horizontally is generally cheaper than dev time .... to a point.
High traffic use cases would benefit more from a more careful performance based selection for server - maybe even C, maybe some sophisticated selection logic of what
type of server serves the data?...

- The next few improvements you’d make, and what challenges might exist
```
- Paging
- Better search interface for files - solr/elasticsearch - find by name..
- code cleanup
- SSO/Auth pattern clarity + lockin
- Customer specific s3 config + s3 keys
- Soft deletes
- better error handling
- Oauth repo - sourced from databases instead of in code - allows custom client config
- React FE
- ... way more tests + more modules to make tests easier to write/mock
Could prove improve to no end without a bounded box
```

## How to run
A few docker files are present in the repo

This should set everything up with an SSO provider + run app with its own postgres instance
```
docker-compose -f authentik/docker-compose.yaml up -d
docker-compose -f docker-compose-run.yml up -d
```

Swagger api docs for easy endpoint interactions
http://localhost:8080/swagger-ui/index.html

There is a stateless version of auth under LoginController in swagger docs with some hardcoded behavior - in place of implementing any user database/roles.
When provided a token from response simply place the value returned in the Authorize spot in swagger
![alt text](./docs/swagger_auth.png)

Hardcoded role list (for now) - code search for
```
var authorities = List.of("ROLE_ORG_1"
```

If SSO is desired it may require some setup if using

http://localhost:9000/if/flow/initial-setup/

You will need to follow instructions of setting up an SSO OIDC provider for authentik - more docs here if needed https://docs.goauthentik.io/
```
Steps:
- create app in authentik
- (should promp to create associated provider)
- select OIDC
- follow prompts
- set redirect URL to http://localhost:8080/login/oauth2/code/aptible
- create groups 
- add user to groups
```

Groups are currently hardcoded when using the basic login (not SSO). T
These would need to be added as groups without the role prefix to the SSO provider
```
var authorities = List.of("ROLE_ORG_1", "ROLE_CAN_UPLOAD", "ROLE_CAN_DOWNLOAD","ROLE_CAN_READ_FILES");
Add the following groups
ORG_1,CAN_UPLOAD ....
```

when provided with clientId/Secret
can update `OauthConfig.java` with the values accordingly



SSO login can be found here - currently application will use a stateful session for SSO (tracked as a cookie in your browser)
http://localhost:8080/dlogin


## DB config
Some config can be found under `application.properties`. To have this auto drop-create schema VS just update schema swap the following in/out
```
spring.jpa.hibernate.ddl-auto=create-drop
#spring.jpa.hibernate.ddl-auto=update
```

Running up on time a bit so pushing this for the time being.... can come back and clean things up time permitting
