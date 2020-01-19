# Pr0gramm Keycrawler

This project is a crawler which tries to extract game keys from pictures which are puplished to the popular german imageboard [Pr0gramm.com](https://pr0gramm.com/). Every found game key is send to all registered users via [Telegram](https://telegram.org/).

# Registration

To use the crawler you have to own a [Pr0gramm-Account](https://pr0gramm.com/). 
Send a private messge to [XMrNiceGuyX](https://pr0gramm.com/inbox/messages/XMrNiceGuyX). The message has to contain the following keyword: **pr0keycrawler**
You will receive a token within 10 minutes. You can use this token agains the Telegram-Bot [pr0grammKeysBot](https://telegram.me/pr0grammKeysBot). Use /authenticate and reply with the received token [username:token].

# Telegram Commands

```
/help - list all available commands
/authenticate - authenticate yourself with the received token
/delete - delete your account
/subscribe - subscribe to receive the crawled keys
/unsubscribe - unsubscribe to stop receiving the crawled keys
```

# Technologies
Dependency Management
* [Maven](https://maven.apache.org/)

Production
* [Java 11](https://adoptopenjdk.net/index.html)
* [Spring Boot 2](https://spring.io/)
* [Spring Web Reactive](https://projectreactor.io/)
* [Tesseract](https://github.com/bytedeco/javacpp-presets/blob/master/tesseract/README.md)
* [OpenCV](https://github.com/bytedeco/javacpp-presets/blob/master/opencv/README.md)
* [Postgres](https://www.postgresql.org/)

Testing
* [Groovy](https://groovy-lang.org/)
* [Spock](spockframework.org/spock/docs/1.3/all_in_one.html)

Deployment
* [Docker](https://www.docker.com/)

## How it works
Every 15 seconds the latest images are crawled. With the [EAST-Algorithm](http://openaccess.thecvf.com/content_cvpr_2017/papers/Zhou_EAST_An_Efficient_CVPR_2017_paper.pdf) all pictures which do not contain text will be thrown away. After the preselection the images are preprocessed with OpenCV to get a better result by Tesseract. Tesseract gets this image and tries to extract the text. This text is matched against a RegEX. If it matches we've found a game key which will be then published to the registered users through telegram.

## Locale Development

If you want to run this crawler locally you need [Java 11](https://adoptopenjdk.net/index.html) and [Maven](https://maven.apache.org/). 

```bash
git clone https://github.com/Chr3is/pr0KeyCrawler.git
cd pr0KeyCrawler
mvn clean install
```
This will download all required dependencies, build all sources and execute the tests.
To run the just created jar use 

`java -jar .target/programmkeycrawler-0.0.1-SNAPSHOT.jar`

This will execute the crawler with the default configuration.

## Configurations

There are some configuration options to control how the app works:

| Property                          	| Type    	| Description                                                                                                                         	| Default         	|
|-----------------------------------	|---------	|-------------------------------------------------------------------------------------------------------------------------------------	|-----------------	|
| pr0gramm.api-client.cookies.me    	| String  	| The pr0gramm cookie to make authenticated requests. If this property is not set the crawler can only access the public images (SFW) 	| null            	|
| pr0gramm.api-client.notifications 	| Boolean 	| If this property is set to true a comment will be posted under the crawled post if a key was found                                  	| false           	|
| scheduler.enabled                 	| Boolean 	| If this property is set to true all scheduling tasks like checking for new registrations or crawling posts will be executed         	| true            	|
| database.in-memory                	| Boolean 	| If this property is set to true a local H2-Database will be used.                                                                   	| true            	|
| database.host                     	| String  	| The database host. Keep in mind that only a Postgres-Database is supported                                                          	| localhost       	|
| database.name                     	| String  	| The name of the database                                                                                                            	| test            	|
| database.user                     	| String  	| The name of the database user                                                                                                       	| sa              	|
| database.password                 	| String  	| The password of the database user                                                                                                   	| ''              	|
| telegram.enabled                  	| Boolean 	| If this property is set to true the crawled keys will be send to all registered users. For this you'll have to create an own [Bot](https://core.telegram.org/bots)                                                	| false           	|
| telegram.creator-id               	| Long    	| The Telegram-Id of the bot owner                                                                                                    	| 12345           	|
| telegram.username                 	| String  	| The name of the telegram bot                                                                                                        	| pr0grammKeysBot 	|
| telegram.token                    	| String  	| The token for the telegram bot                                                                                                      	| test            	|
| sentry.dsn                        	| String  	| The dsn for sentry where exceptions are reported                                                                                    	| null            	|

To set a property when executing the jar directly use the property as environment variable. Keep in mind that everything has to be written in uppercase and `.` needs to be a `_` and the`-` has to be removed.

Powershell:
```
$env:DATABASE_INMEMORY="false"
java -jar .target/programmkeycrawler-0.0.1-SNAPSHOT.jar
```
Bash:
```
export DATABASE_INMEMORY=false
java -jar .target/programmkeycrawler-0.0.1-SNAPSHOT.jar
```

## Docker

You will have to install docker on your locale machine. To create the image run the following command:

`mvn clean install -DskipTests docker:build "-Dos.detected.classifier=linux-x86_64"`

This will create a linux based image with all required dependencies and packages. To start the container use: 

`docker run -it ingagnable/programmkeycrawler`

If you want to change the default behavior (default configuration) you can pass environment variables as well. See [Docker](https://docs.docker.com/engine/reference/commandline/run/)

## Contributing

Feel free to create issues if you find bugs or if you want to request new features or improvments.
